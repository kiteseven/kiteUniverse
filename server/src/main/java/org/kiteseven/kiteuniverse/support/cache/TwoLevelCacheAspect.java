package org.kiteseven.kiteuniverse.support.cache;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.kiteseven.kiteuniverse.config.properties.TwoLevelCacheProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Applies two-level cache read-through and invalidation behavior around annotated methods.
 */
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class TwoLevelCacheAspect {

    private static final Logger log = LoggerFactory.getLogger(TwoLevelCacheAspect.class);

    private final TwoLevelCacheService twoLevelCacheService;
    private final TwoLevelCacheProperties twoLevelCacheProperties;
    private final ObjectMapper objectMapper;
    private final ConfigurableBeanFactory beanFactory;
    private final ExpressionParser expressionParser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    public TwoLevelCacheAspect(TwoLevelCacheService twoLevelCacheService,
                               TwoLevelCacheProperties twoLevelCacheProperties,
                               ObjectMapper objectMapper,
                               ConfigurableBeanFactory beanFactory) {
        this.twoLevelCacheService = twoLevelCacheService;
        this.twoLevelCacheProperties = twoLevelCacheProperties;
        this.objectMapper = objectMapper;
        this.beanFactory = beanFactory;
    }

    @Around("@annotation(twoLevelCache)")
    public Object around(ProceedingJoinPoint proceedingJoinPoint, TwoLevelCache twoLevelCache) throws Throwable {
        if (twoLevelCache.mode() == TwoLevelCache.Mode.EVICT) {
            return evictAfterInvocation(proceedingJoinPoint, twoLevelCache);
        }
        return loadWithTwoLevelCache(proceedingJoinPoint, twoLevelCache);
    }

    private Object loadWithTwoLevelCache(ProceedingJoinPoint proceedingJoinPoint,
                                         TwoLevelCache twoLevelCache) throws Throwable {
        if (!twoLevelCacheService.isEnabled()) {
            return proceedingJoinPoint.proceed();
        }

        Method method = resolveMethod(proceedingJoinPoint);
        String cacheKey = resolveKey(twoLevelCache.key(), method, proceedingJoinPoint);
        if (!StringUtils.hasText(cacheKey)) {
            return proceedingJoinPoint.proceed();
        }

        Duration localTtl = resolveLocalTtl(twoLevelCache);
        JavaType returnType = objectMapper.getTypeFactory().constructType(method.getGenericReturnType());
        Object cachedValue = twoLevelCacheService.get(cacheKey, returnType, localTtl);
        if (cachedValue != null) {
            return cachedValue;
        }

        Object loadedValue = proceedingJoinPoint.proceed();
        if (loadedValue != null || twoLevelCache.cacheNullValue()) {
            twoLevelCacheService.put(cacheKey, loadedValue, localTtl, resolveRedisTtl(twoLevelCache));
        }
        return loadedValue;
    }

    private Object evictAfterInvocation(ProceedingJoinPoint proceedingJoinPoint,
                                        TwoLevelCache twoLevelCache) throws Throwable {
        Object result = proceedingJoinPoint.proceed();
        Method method = resolveMethod(proceedingJoinPoint);
        List<String> cacheKeys = resolveKeys(twoLevelCache.evictKeys(), method, proceedingJoinPoint);
        if (cacheKeys.isEmpty()) {
            return result;
        }

        if (TransactionSynchronizationManager.isSynchronizationActive()
                && TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    evictKeys(cacheKeys);
                }
            });
            return result;
        }

        evictKeys(cacheKeys);
        return result;
    }

    private void evictKeys(List<String> cacheKeys) {
        for (String cacheKey : cacheKeys) {
            try {
                twoLevelCacheService.evict(cacheKey);
            } catch (Exception exception) {
                log.warn("Two-level cache eviction skipped for key {}", cacheKey, exception);
            }
        }
    }

    private Method resolveMethod(ProceedingJoinPoint proceedingJoinPoint) {
        MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
        return AopUtils.getMostSpecificMethod(methodSignature.getMethod(), proceedingJoinPoint.getTarget().getClass());
    }

    private List<String> resolveKeys(String[] expressions, Method method, ProceedingJoinPoint proceedingJoinPoint) {
        Set<String> resolvedKeys = new LinkedHashSet<>();
        if (expressions == null) {
            return List.of();
        }
        for (String expression : expressions) {
            String resolvedKey = resolveKey(expression, method, proceedingJoinPoint);
            if (StringUtils.hasText(resolvedKey)) {
                resolvedKeys.add(resolvedKey);
            }
        }
        return new ArrayList<>(resolvedKeys);
    }

    private String resolveKey(String expression, Method method, ProceedingJoinPoint proceedingJoinPoint) {
        Object resolvedValue = evaluateExpression(expression, method, proceedingJoinPoint);
        if (resolvedValue == null) {
            return null;
        }
        String resolvedKey = String.valueOf(resolvedValue).trim();
        return StringUtils.hasText(resolvedKey) ? resolvedKey : null;
    }

    private Object evaluateExpression(String expression, Method method, ProceedingJoinPoint proceedingJoinPoint) {
        if (!StringUtils.hasText(expression)) {
            return null;
        }

        String trimmedExpression = expression.trim();
        if (!mightBeExpression(trimmedExpression)) {
            return trimmedExpression;
        }

        if (method == null || proceedingJoinPoint == null) {
            StandardEvaluationContext evaluationContext = new StandardEvaluationContext(this);
            evaluationContext.setBeanResolver(new BeanFactoryResolver(beanFactory));
            return expressionParser.parseExpression(trimmedExpression).getValue(evaluationContext);
        }

        MethodBasedEvaluationContext evaluationContext = new MethodBasedEvaluationContext(
                proceedingJoinPoint.getTarget(),
                method,
                proceedingJoinPoint.getArgs(),
                parameterNameDiscoverer
        );
        evaluationContext.setBeanResolver(new BeanFactoryResolver(beanFactory));
        return expressionParser.parseExpression(trimmedExpression).getValue(evaluationContext);
    }

    private boolean mightBeExpression(String expression) {
        return expression.contains("#")
                || expression.contains("@")
                || expression.contains("T(")
                || expression.startsWith("'")
                || expression.startsWith("\"");
    }

    private Duration resolveLocalTtl(TwoLevelCache twoLevelCache) {
        Long localTtlOverride = resolveDurationOverrideSeconds(twoLevelCache.localTtlExpression());
        return Duration.ofSeconds(resolveTtlSeconds(
                localTtlOverride == null ? twoLevelCache.localTtlSeconds() : localTtlOverride,
                twoLevelCacheProperties.getDefaultLocalTtlSeconds()
        ));
    }

    private Duration resolveRedisTtl(TwoLevelCache twoLevelCache) {
        Long redisTtlOverride = resolveDurationOverrideSeconds(twoLevelCache.redisTtlExpression());
        return Duration.ofSeconds(resolveTtlSeconds(
                redisTtlOverride == null ? twoLevelCache.redisTtlSeconds() : redisTtlOverride,
                twoLevelCacheProperties.getDefaultRedisTtlSeconds()
        ));
    }

    private Long resolveDurationOverrideSeconds(String expression) {
        if (!StringUtils.hasText(expression)) {
            return null;
        }
        Object resolvedValue = evaluateExpression(expression, null, null);
        if (resolvedValue == null) {
            return null;
        }
        return Long.parseLong(String.valueOf(resolvedValue).trim());
    }

    private long resolveTtlSeconds(long overrideTtlSeconds, long defaultTtlSeconds) {
        long candidate = overrideTtlSeconds >= 0L ? overrideTtlSeconds : defaultTtlSeconds;
        return Math.max(candidate, 1L);
    }
}
