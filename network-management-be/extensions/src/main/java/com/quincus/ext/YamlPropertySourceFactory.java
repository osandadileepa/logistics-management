package com.quincus.ext;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.Properties;

@Slf4j
public class YamlPropertySourceFactory implements PropertySourceFactory {

    @Override
    public PropertySource<?> createPropertySource(@Nullable String name, EncodedResource resource) {
        final Properties propertiesFromYaml = loadYamlIntoProperties(resource);
        final String sourceName = StringUtils.isNotBlank(name) ? name : resource.getResource().getFilename();
        Assert.notNull(sourceName, "sourceName should not be null!");
        Assert.notNull(propertiesFromYaml, "propertiesFromYaml should not be null");
        return new PropertiesPropertySource(sourceName, propertiesFromYaml);
    }

    private Properties loadYamlIntoProperties(EncodedResource resource) {
        try {
            final YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
            factory.setResources(resource.getResource());
            factory.afterPropertiesSet();
            return factory.getObject();
        } catch (IllegalStateException e) {
            log.warn(e.getMessage());
            return new Properties();
        }
    }

}
