package com.techtron.onebook.module.app.service.logistics;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "onebook.logistics")
public class LogisticsProperties {
    private boolean enabled;
    private String customer = "";
    private String key = "";
}
