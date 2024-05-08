package com.quincus.qportal.config;

import com.quincus.ext.YamlPropertySourceFactory;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@ConfigurationProperties(prefix = "qportal")
@Data
@Configuration
@PropertySource(value = {"classpath:config/qportal-config-${spring.profiles.active}.yml"}
        , factory = YamlPropertySourceFactory.class)
public class QPortalProperties {
    private String s2sToken;
    private String baseUrl;
    private String packageTypesAPI;
    private String partnersAPI;
    private String locationsAPI;
    private String locationTypesAPI;
    private String serviceTypeAPI;
    private String listUsersAPI;
    private String usersAPI;
    private String usersGetMyProfileApi;
    private String milestonesAPI;
    private String costTypesAPI;
    private String currenciesAPI;
    private String vehiclesAPI;
    private String vehicleTypesAPI;
    private String notificationAPI;
    private String facilitiesAPI;
    private String tagsAPI;
    private String organizationAPI;
    @Value("${spring.profiles.active}")
    private String profile;
}
