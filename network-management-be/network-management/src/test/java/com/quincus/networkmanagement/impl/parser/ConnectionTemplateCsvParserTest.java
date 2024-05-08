package com.quincus.networkmanagement.impl.parser;

import com.quincus.networkmanagement.impl.attachment.connection.ConnectionRecord;
import com.quincus.networkmanagement.impl.attachment.connection.parser.ConnectionTemplateCSVParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.quincus.networkmanagement.impl.data.NetworkManagementRecordTestData.dummyAirConnectionRecord;
import static com.quincus.networkmanagement.impl.data.NetworkManagementRecordTestData.dummyConnectionsCsvTemplate;
import static com.quincus.networkmanagement.impl.data.NetworkManagementRecordTestData.dummyGroundConnectionRecord;
import static org.assertj.core.api.Assertions.assertThat;

class ConnectionTemplateCsvParserTest {

    ConnectionTemplateCSVParser parser = new ConnectionTemplateCSVParser();

    @Test
    @DisplayName("GIVEN valid template WHEN parse CSV file THEN return expected")
    void returnExpectedWhenValidCsvTemplate() {
        MultipartFile template = dummyConnectionsCsvTemplate();
        List<ConnectionRecord> result = parser.parseFile(template);

        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(List.of(dummyGroundConnectionRecord(), dummyAirConnectionRecord()));
    }
}
