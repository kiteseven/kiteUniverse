package org.kiteseven.kiteuniverse.support.cache;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiteseven.kiteuniverse.config.properties.CommunityContentProperties;
import org.kiteseven.kiteuniverse.config.properties.TwoLevelCacheProperties;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.lang.reflect.Method;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TwoLevelCacheAspectTest {

    @Mock
    private TwoLevelCacheService twoLevelCacheService;

    private TwoLevelCacheAspect twoLevelCacheAspect;

    @BeforeEach
    void setUp() {
        TwoLevelCacheProperties cacheProperties = new TwoLevelCacheProperties();
        cacheProperties.setDefaultLocalTtlSeconds(30L);
        cacheProperties.setDefaultRedisTtlSeconds(300L);

        CommunityContentProperties communityContentProperties = new CommunityContentProperties();
        communityContentProperties.setHomeLocalCacheSeconds(12L);
        communityContentProperties.setHomeCacheSeconds(120L);

        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerSingleton("communityContentProperties", communityContentProperties);

        twoLevelCacheAspect = new TwoLevelCacheAspect(
                twoLevelCacheService,
                cacheProperties,
                new ObjectMapper(),
                beanFactory
        );
    }

    @Test
    void cacheHitShouldBypassMethodInvocation() throws Throwable {
        DemoService target = new DemoService();
        Method method = DemoService.class.getMethod("loadHome", Long.class);
        ProceedingJoinPoint joinPoint = mockJoinPoint(target, method, new Object[]{42L}, "loaded-42");

        when(twoLevelCacheService.isEnabled()).thenReturn(true);
        when(twoLevelCacheService.get(eq("content:42"), any(JavaType.class), eq(Duration.ofSeconds(12))))
                .thenReturn("cached-42");

        Object result = twoLevelCacheAspect.around(joinPoint, method.getAnnotation(TwoLevelCache.class));

        assertEquals("cached-42", result);
        verify(twoLevelCacheService).get(eq("content:42"), any(JavaType.class), eq(Duration.ofSeconds(12)));
        verify(joinPoint, never()).proceed();
        verify(twoLevelCacheService, never()).put(any(), any(), any(), any());
    }

    @Test
    void cacheMissShouldLoadAndPopulateBothCacheLevels() throws Throwable {
        DemoService target = new DemoService();
        Method method = DemoService.class.getMethod("loadHome", Long.class);
        ProceedingJoinPoint joinPoint = mockJoinPoint(target, method, new Object[]{42L}, "loaded-42");

        when(twoLevelCacheService.isEnabled()).thenReturn(true);
        when(twoLevelCacheService.get(eq("content:42"), any(JavaType.class), eq(Duration.ofSeconds(12))))
                .thenReturn(null);

        Object result = twoLevelCacheAspect.around(joinPoint, method.getAnnotation(TwoLevelCache.class));

        assertEquals("loaded-42", result);
        verify(joinPoint, times(1)).proceed();
        verify(twoLevelCacheService).put(
                "content:42",
                "loaded-42",
                Duration.ofSeconds(12),
                Duration.ofSeconds(120)
        );
    }

    @Test
    void evictModeShouldResolveAndDeduplicateKeys() throws Throwable {
        DemoService target = new DemoService();
        Method method = DemoService.class.getMethod("refreshContent", Long.class);
        ProceedingJoinPoint joinPoint = mockJoinPoint(target, method, new Object[]{42L}, "done");

        Object result = twoLevelCacheAspect.around(joinPoint, method.getAnnotation(TwoLevelCache.class));

        assertEquals("done", result);
        verify(twoLevelCacheService).evict("content:home");
        verify(twoLevelCacheService, times(1)).evict("content:42");
    }

    private ProceedingJoinPoint mockJoinPoint(Object target,
                                              Method method,
                                              Object[] args,
                                              Object proceedResult) throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature methodSignature = mock(MethodSignature.class);

        when(joinPoint.getTarget()).thenReturn(target);
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        lenient().when(joinPoint.proceed()).thenReturn(proceedResult);
        return joinPoint;
    }

    private static class DemoService {

        @TwoLevelCache(
                key = "'content:' + #p0",
                localTtlExpression = "@communityContentProperties.homeLocalCacheSeconds",
                redisTtlExpression = "@communityContentProperties.homeCacheSeconds"
        )
        public String loadHome(Long id) {
            return "loaded-" + id;
        }

        @TwoLevelCache(
                mode = TwoLevelCache.Mode.EVICT,
                evictKeys = {"'content:home'", "'content:' + #p0", "'content:' + #p0"}
        )
        public String refreshContent(Long id) {
            return "done-" + id;
        }
    }
}
