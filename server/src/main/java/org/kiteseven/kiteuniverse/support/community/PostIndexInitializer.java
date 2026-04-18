package org.kiteseven.kiteuniverse.support.community;

import org.kiteseven.kiteuniverse.service.PostIndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 应用启动时全量重建 Elasticsearch 帖子索引。
 * 优先级 300，在数据初始化（Order 200）之后执行。
 * ES 不可用时仅打印警告，不阻断启动流程。
 */
@Component
@Order(300)
public class PostIndexInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PostIndexInitializer.class);

    private final PostIndexService postIndexService;

    public PostIndexInitializer(PostIndexService postIndexService) {
        this.postIndexService = postIndexService;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            postIndexService.rebuildAll();
        } catch (Exception e) {
            log.warn("[ES] Startup index rebuild failed, search will fall back to SQL: {}", e.getMessage());
        }
    }
}
