package com.techtron.onebook.framework.signature.config;

import com.techtron.onebook.framework.redis.config.OnebookRedisAutoConfiguration;
import com.techtron.onebook.framework.signature.core.aop.ApiSignatureAspect;
import com.techtron.onebook.framework.signature.core.redis.ApiSignatureRedisDAO;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * HTTP API 签名的自动配置类
 *
 * @author Zhougang
 */
@AutoConfiguration(after = OnebookRedisAutoConfiguration.class)
public class OnebookApiSignatureAutoConfiguration {

    @Bean
    public ApiSignatureAspect signatureAspect(ApiSignatureRedisDAO signatureRedisDAO) {
        return new ApiSignatureAspect(signatureRedisDAO);
    }

    @Bean
    public ApiSignatureRedisDAO signatureRedisDAO(StringRedisTemplate stringRedisTemplate) {
        return new ApiSignatureRedisDAO(stringRedisTemplate);
    }

}
