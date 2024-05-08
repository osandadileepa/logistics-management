package com.quincus.web.common.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.quincus.web.common.serializer.BigDecimalDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Configuration
public class JacksonConfiguration {

    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    @Bean
    @Primary
    public ObjectMapper buildObjectMapper() {
        return new Jackson2ObjectMapperBuilder()
                .modules(new JavaTimeModule())
                .serializers(new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_FORMAT)))
                .deserializerByType(BigDecimal.class, new BigDecimalDeserializer())
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .featuresToEnable(
                        SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                        SerializationFeature.WRITE_ENUMS_USING_TO_STRING
                )
                .featuresToDisable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .build();
    }
}
