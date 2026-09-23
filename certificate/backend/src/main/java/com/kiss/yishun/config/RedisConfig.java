package com.kiss.yishun.config;

import com.kiss.yishun.utils.StrUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
@PropertySource("classpath:redis.properties")
@Log4j2
public class RedisConfig {

    @Value("${spring.redis.jedis.pool.max-idle}")
    private int maxIdle;

    @Value("${spring.redis.jedis.pool.min-idle}")
    private int minIdle;

    @Value("${spring.redis.jedis.pool.max-active}")
    private int maxActive;

    @Value("${spring.redis.jedis.pool.max-wait}")
    private int maxWaitMillis;

    @Value("${spring.redis.block-when-exhausted}")
    private boolean  blockWhenExhausted;

    public JedisPool createJedisConnectionFactory(int dbIndex, String host, int port, String password, int timeout) {
        JedisPoolConfig poolConfig = setPoolConfig(maxIdle, minIdle, maxActive, maxWaitMillis, true);
        if (StrUtils.isEmpty(password)) {
            return new JedisPool(poolConfig, host, port, timeout);
        }
        JedisPool jedisPool = new JedisPool(poolConfig, host, port, timeout, password,dbIndex);
        return jedisPool;

    }

    public JedisPoolConfig setPoolConfig(int maxIdle, int minIdle, int maxActive, int maxWait, boolean testOnBorrow) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMinIdle(minIdle);
        poolConfig.setMaxTotal(maxActive);
        poolConfig.setMaxWaitMillis(maxWait);
        poolConfig.setTestOnBorrow(testOnBorrow);
        // 连接耗尽时是否阻塞, false报异常,ture阻塞直到超时, 默认true
        poolConfig.setBlockWhenExhausted(blockWhenExhausted);
        // 是否启用pool的jmx管理功能, 默认true
        poolConfig.setJmxEnabled(true);
        return poolConfig;
    }
}

