package org.kiteseven.kiteuniverse.support.notification;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class NotificationDeliveryModeFallbackTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(
                    DirectNotificationCommandPublisher.class,
                    RabbitNotificationCommandPublisher.class,
                    NotificationCommandListener.class,
                    org.kiteseven.kiteuniverse.config.NotificationMqConfig.class
            )
            .withBean(NotificationCommandProcessor.class, () -> mock(NotificationCommandProcessor.class));

    @Test
    void shouldFallbackToDirectPublisherWhenBrokerIsUnavailable() {
        contextRunner
                .withPropertyValues(
                        "kite-universe.notification.mq.enabled=true",
                        "spring.rabbitmq.host=127.0.0.1",
                        "spring.rabbitmq.port=1"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(NotificationCommandPublisher.class);
                    assertThat(context).hasSingleBean(DirectNotificationCommandPublisher.class);
                    assertThat(context).doesNotHaveBean(RabbitNotificationCommandPublisher.class);
                    assertThat(context).doesNotHaveBean(NotificationCommandListener.class);
                });
    }

    @Test
    void shouldUseDirectPublisherWhenMqIsDisabled() {
        contextRunner
                .withPropertyValues("kite-universe.notification.mq.enabled=false")
                .run(context -> {
                    assertThat(context).hasSingleBean(NotificationCommandPublisher.class);
                    assertThat(context).hasSingleBean(DirectNotificationCommandPublisher.class);
                    assertThat(context).doesNotHaveBean(RabbitNotificationCommandPublisher.class);
                });
    }
}
