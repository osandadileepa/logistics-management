package test.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;

@Slf4j
public class TestUtil {
    private static final TestUtil INSTANCE = new TestUtil();
    @Getter
    private final ObjectMapper objectMapper;

    public static TestUtil getInstance() {
        return INSTANCE;
    }

    private TestUtil() {
        final Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        builder.modules(new JavaTimeModule());
        builder.propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        builder.serializationInclusion(JsonInclude.Include.NON_NULL);
        builder.featuresToEnable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        builder.featuresToDisable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper = builder.build();
    }

    public JsonNode getDataFromFile(String jsonPath) {
        try {
            ClassPathResource path = new ClassPathResource(jsonPath);
            return this.objectMapper.readValue(path.getFile(), JsonNode.class);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return this.objectMapper.createObjectNode();
    }
}
