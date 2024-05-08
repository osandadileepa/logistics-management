package com.quincus.s3.config;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.quincus.ext.YamlPropertySourceFactory;
import com.quincus.s3.domain.S3Bucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = {"classpath:config/s3-integration-config-${spring.profiles.active}.yml"},
        factory = YamlPropertySourceFactory.class)
@EnableConfigurationProperties({UrlExpiryProperties.class, S3ArrayProperties.class})
public class S3IntegrationConfiguration {

    @Value("${s3-integration.region-name}")
    private String regionName;
    @Value("${s3-integration.bucket-name}")
    private String bucketName;
    @Value("${s3-integration.base-dir}")
    private String baseDir;

    @Bean
    @Profile("!local")
    public AmazonS3 s3client() {
        return AmazonS3ClientBuilder
                .standard()
                .withRegion(regionName)
                .build();
    }

    @Bean
    public S3Bucket s3bucket(UrlExpiryProperties urlExpiryProperties,
                             S3ArrayProperties s3ArrayProperties) {
        S3Bucket bucket = new S3Bucket(Integer.parseInt(urlExpiryProperties.getUpload()), Integer.parseInt(urlExpiryProperties.getRead()));
        bucket.setName(bucketName);
        bucket.setBaseDir(baseDir);

        bucket.recalculatePreSignedUploadUrlExpiryDate();
        bucket.recalculatePreSignedReadFileUrlExpiryDate();

        bucket.setSupportedMediaTypes(s3ArrayProperties.getSupportedMediaTypes());
        bucket.setAllowedSubDirectories(s3ArrayProperties.getAllowedSubdirectories());
        return bucket;
    }
}
