package com.kiss.yishun.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import javax.annotation.PostConstruct;

@Configuration
@Data
@PropertySources({
    @PropertySource(value = "classpath:sms.properties", ignoreResourceNotFound = true),
    @PropertySource(value = "classpath:sms-${spring.profiles.active}.properties", ignoreResourceNotFound = true)
})
public class SmsConfig {

    @Value("${sms.open}")
    private int smsOpen;

    @Value("${tencent.apiUrl}")
    private String apiUrl;

    @Value("${tencent.appId}")
    private String tencentAppId;

    @Value("${tencent.secretId}")
    private String tencentSecretId;

    @Value("${tencent.secretkey}")
    private String tencentSecretkey;

    @Value("${tencent.region}")
    private String tencentRegion;

    private String tencentSignName = "杭州一瞬宇宙收藏品";

    @Value("${tencent.registTemplateId}")
    private String tencentRegistTemplateId;

    @Value("${tencent.modifyPhoneTemplateId}")
    private String tencentModifyPhoneTemplateId;

    // 测试赋值
//    @PostConstruct public void init() {
//        System.out.println(tencentAppId);
//    }
}
