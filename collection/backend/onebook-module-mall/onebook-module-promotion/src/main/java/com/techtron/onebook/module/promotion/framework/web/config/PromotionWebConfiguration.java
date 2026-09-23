package com.techtron.onebook.module.promotion.framework.web.config;

import com.techtron.onebook.framework.swagger.config.OnebookSwaggerAutoConfiguration;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * promotion 模块的 web 组件的 Configuration
 *
 * @author 芋道源码
 */
@Configuration(proxyBeanMethods = false)
public class PromotionWebConfiguration {

    /**
     * promotion 模块的 API 分组
     */
    @Bean
    public GroupedOpenApi promotionGroupedOpenApi() {
        return OnebookSwaggerAutoConfiguration.buildGroupedOpenApi("promotion");
    }

}
