package com.quincus.networkmanagement.impl.db;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.vladmihalcea.hibernate.type.util.ObjectMapperSupplier;

/**
 * This class is responsible for serializing/deserializing all JSON-related columns in our hibernate entities.
 * We also need to define our custom Object Mapper as the primary object mapper
 * is yet to be created upon creation of this class.
 **/
public class HibernateObjectMapperSupplier implements ObjectMapperSupplier {
    private static final ObjectMapper HIBERNATE_OBJECT_MAPPER = new ObjectMapper();

    static {
        HIBERNATE_OBJECT_MAPPER.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        HIBERNATE_OBJECT_MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        HIBERNATE_OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        HIBERNATE_OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        HIBERNATE_OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public ObjectMapper get() {
        return HIBERNATE_OBJECT_MAPPER;
    }
}