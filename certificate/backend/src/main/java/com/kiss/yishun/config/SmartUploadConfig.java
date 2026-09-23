package com.kiss.yishun.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@Configuration
@Data
@PropertySources({
    @PropertySource(value = "classpath:smartupload.properties", ignoreResourceNotFound = true),
    @PropertySource(value = "classpath:smartupload-${spring.profiles.active}.properties", ignoreResourceNotFound = true)
})
public class SmartUploadConfig {

    @Value("${smartupload.disk.preciousdir}")
    private String diskPreciousDir;

    @Value("${smartupload.disk.tmpdir}")
    private String diskTmpDir;

    @Value("${smartupload.disk.zipdir}")
    private String diskZipDir;

    @Value("${smartupload.return.tmpdir}")
    private String returnTmpDir;

    @Value("${smartupload.return.preciousdir}")
    private String returnPreciousDir;

    @Value("${smartupload.return.zipdir}")
    private String returnZipDir;

    @Value("${smartupload.port}")
    private String port;

    @Value("${smartupload.server.name}")
    private String serverName;
}
