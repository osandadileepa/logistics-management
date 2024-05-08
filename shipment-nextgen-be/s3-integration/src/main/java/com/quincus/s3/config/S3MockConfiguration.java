package com.quincus.s3.config;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.quincus.ext.YamlPropertySourceFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Profile("local")
@PropertySource(value = {"classpath:config/s3-integration-config-local.yml"},
        factory = YamlPropertySourceFactory.class)
@EnableConfigurationProperties({UrlExpiryProperties.class, S3ArrayProperties.class})
public class S3MockConfiguration {
    private static final String DUMMY_ORG_ID = "karate-org";
    private static final String DUMMY_DIRECTORY = "shipment_attachments";
    private static final String DUMMY_FILENAME = "karate-test-sample-file.jpg";
    private static final String DUMMY_CONTENTS = "Testing Only.";
    @Value("${s3-integration.region-name}")
    private String regionName;
    @Value("${s3-integration.bucket-name}")
    private String bucketName;
    @Value("${s3-integration.mock-server}")
    private String mockServer;

    @Bean
    public AmazonS3 s3client() {
        AmazonS3 client = AmazonS3ClientBuilder
                .standard()
                .withPathStyleAccessEnabled(true)
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(mockServer, regionName))
                .build();
        client.createBucket(bucketName);
        client.putObject(bucketName, String.format("local/%s/%s/%s", DUMMY_ORG_ID, DUMMY_DIRECTORY, DUMMY_FILENAME),
                DUMMY_CONTENTS);
        return client;
    }
}
