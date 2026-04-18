package org.kiteseven.kiteuniverse.config.properties;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class CommunityContentPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
            .withUserConfiguration(CommunityContentProperties.class)
            .withPropertyValues(
                    "kite-universe.content.home-cache-seconds=120",
                    "kite-universe.content.home-local-cache-seconds=15",
                    "kite-universe.content.boards-cache-seconds=240",
                    "kite-universe.content.boards-local-cache-seconds=25"
            );

    @Test
    void shouldExposeExpectedBeanNameAndBindValues() {
        contextRunner.run(context -> {
            assertThat(context).hasBean("communityContentProperties");

            CommunityContentProperties properties =
                    context.getBean("communityContentProperties", CommunityContentProperties.class);

            assertThat(properties.getHomeCacheSeconds()).isEqualTo(120L);
            assertThat(properties.getHomeLocalCacheSeconds()).isEqualTo(15L);
            assertThat(properties.getBoardsCacheSeconds()).isEqualTo(240L);
            assertThat(properties.getBoardsLocalCacheSeconds()).isEqualTo(25L);
        });
    }
}
