package com.quincus.s3.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "s3-integration")
public class S3ArrayProperties {
    private Map<String, List<String>> supportedMediaTypes;
    private List<String> allowedSubdirectories;
}
