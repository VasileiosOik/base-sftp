package com.connection.basesftp.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.integration.redis.metadata.RedisMetadataStore;

@Configuration
public class SftpRedisConfiguration {

    private final JedisConnectionFactory jedisConnectionFactory;

    @Autowired
    public SftpRedisConfiguration(JedisConnectionFactory jedisConnectionFactory) {
        this.jedisConnectionFactory = jedisConnectionFactory;
    }

    @Bean
    public RedisMetadataStore redisMetadataStore() {
        return new RedisMetadataStore(jedisConnectionFactory);
    }
}
