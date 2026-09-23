package com.kiss.yishun.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@Configuration
@Data
@PropertySources({
    @PropertySource(value = "classpath:upload.properties", ignoreResourceNotFound = true),
    @PropertySource(value = "classpath:upload-${spring.profiles.active}.properties", ignoreResourceNotFound = true)
})
public class UploadConfig {

    @Value("${upload.disk.preciousdir}")
    private String diskPreciousDir;

    @Value("${upload.disk.tmpdir}")
    private String diskTmpDir;

    @Value("${upload.disk.zipdir}")
    private String diskZipDir;

    @Value("${upload.return.tmpdir}")
    private String returnTmpDir;

    @Value("${upload.return.preciousdir}")
    private String returnPreciousDir;

    @Value("${upload.return.zipdir}")
    private String returnZipDir;

    @Value("${upload.port}")
    private String port;

    @Value("${upload.server.name}")
    private String serverName;
}
