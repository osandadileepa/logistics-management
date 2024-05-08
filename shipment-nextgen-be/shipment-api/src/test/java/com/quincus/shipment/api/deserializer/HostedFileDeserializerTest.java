package com.quincus.shipment.api.deserializer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.quincus.ext.DateTimeUtil;
import com.quincus.shipment.api.domain.HostedFile;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class HostedFileDeserializerTest {

    private static ObjectMapper OBJECTMAPPER;

    @InjectMocks
    private HostedFileDeserializer hostedFileDeserializer;

    @BeforeAll
    static void initialize() {
        final Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        builder.modules(new JavaTimeModule());
        final ObjectMapper objectMapper = builder.build();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        OBJECTMAPPER = objectMapper;
    }

    private static Stream<Arguments> provideHostedFileJsonAndExpectedOutput() {
        ObjectNode node1 = OBJECTMAPPER.createObjectNode();
        node1.put("id", "file-id");

        HostedFile hf1 = new HostedFile();
        hf1.setId("file-id");

        ObjectNode node2 = OBJECTMAPPER.createObjectNode();
        node2.put("file_name", "file-name");

        HostedFile hf2 = new HostedFile();
        hf2.setFileName("file-name");

        ObjectNode node3 = OBJECTMAPPER.createObjectNode();
        node3.put("file_url", "file-url");

        HostedFile hf3 = new HostedFile();
        hf3.setFileUrl("file-url");

        ObjectNode node4 = OBJECTMAPPER.createObjectNode();
        node4.put("file_size", "100");

        HostedFile hf4 = new HostedFile();
        hf4.setFileSize(100L);

        ObjectNode node5 = OBJECTMAPPER.createObjectNode();
        node5.put("file_timestamp", "2023-10-06T11:28:25+11:00");

        HostedFile hf5 = new HostedFile();
        hf5.setFileTimestamp(DateTimeUtil.toFormattedOffsetDateTime("2023-10-06T11:28:25+11:00"));

        return Stream.of(
                Arguments.of(node1, hf1),
                Arguments.of(node2, hf2),
                Arguments.of(node3, hf3),
                Arguments.of(node4, hf4),
                Arguments.of(node5, hf5)
        );
    }

    @ParameterizedTest
    @MethodSource("provideHostedFileJsonAndExpectedOutput")
    void deserialize_hostedFileAsJson_shouldReturnHostedFile(JsonNode hostedFileJson, HostedFile expectedHostedFile) {
        HostedFile result = OBJECTMAPPER.convertValue(hostedFileJson, HostedFile.class);
        assertThat(result.getId()).isEqualTo(expectedHostedFile.getId());
        assertThat(result.getFileName()).isEqualTo(expectedHostedFile.getFileName());
        assertThat(result.getFileUrl()).isEqualTo(expectedHostedFile.getFileUrl());
        assertThat(result.getFileSize()).isEqualTo(expectedHostedFile.getFileSize());
        assertThat(result.getFileTimestamp()).isEqualTo(expectedHostedFile.getFileTimestamp());
    }
}
