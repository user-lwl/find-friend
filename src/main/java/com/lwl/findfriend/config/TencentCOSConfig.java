package com.lwl.findfriend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "tencent-cos")
@Getter
@Setter
public class TencentCOSConfig {
    private String secretId;

    private String secretKey;

    private String AppId;

    private String bucketName;

    private String bucketRegion;

    private String path;

    private Long maxFileSize;
}
