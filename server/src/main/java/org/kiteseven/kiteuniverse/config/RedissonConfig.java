package org.kiteseven.kiteuniverse.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Creates the Redisson client used by Redis Bloom filters and distributed locks.
 */
@Configuration
public class RedissonConfig {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(@Value("${spring.data.redis.host}") String host,
                                         @Value("${spring.data.redis.port}") int port,
                                         @Value("${spring.data.redis.database:0}") int database,
                                         @Value("${spring.data.redis.username:}") String username,
                                         @Value("${spring.data.redis.password:}") String password) {
        Config config = new Config();
        SingleServerConfig singleServerConfig = config.useSingleServer()
                .setAddress("redis://" + host + ":" + port)
                .setDatabase(database);

        if (StringUtils.hasText(username)) {
            singleServerConfig.setUsername(username.trim());
        }
        if (StringUtils.hasText(password)) {
            singleServerConfig.setPassword(password.trim());
        }

        return Redisson.create(config);
    }
}
