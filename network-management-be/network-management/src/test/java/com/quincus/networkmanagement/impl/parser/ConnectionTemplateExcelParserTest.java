package com.quincus.networkmanagement.impl.parser;

import com.quincus.networkmanagement.api.exception.InvalidTemplateException;
import com.quincus.networkmanagement.impl.attachment.connection.ConnectionRecord;
import com.quincus.networkmanagement.impl.attachment.connection.parser.ConnectionTemplateExcelParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.quincus.networkmanagement.impl.data.NetworkManagementRecordTestData.dummyAirConnectionRecord;
import static com.quincus.networkmanagement.impl.data.NetworkManagementRecordTestData.dummyConnectionsExcelTemplate;
import static com.quincus.networkmanagement.impl.data.NetworkManagementRecordTestData.dummyGroundConnectionRecord;
import static com.quincus.networkmanagement.impl.data.NetworkManagementRecordTestData.dummyTemplate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class ConnectionTemplateExcelParserTest {

    ConnectionTemplateExcelParser parser = new ConnectionTemplateExcelParser();

    @Test
    @DisplayName("GIVEN valid template WHEN parse XLS file THEN return expected")
    void returnExpectedWhenValidExcelTemplate() {
        MultipartFile template = dummyConnectionsExcelTemplate();
        List<ConnectionRecord> result = parser.parseFile(template);

        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(List.of(dummyGroundConnectionRecord(), dummyAirConnectionRecord()));
    }

    @Test
    @DisplayName("GIVEN invalid template WHEN parse XLS file THEN throw error")
    void throwErrorWhenInvalidTemplate() {
        MultipartFile template = dummyTemplate(
                "invalid-template.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );
        assertThatThrownBy(() -> parser.parseFile(template))
                .isInstanceOfSatisfying(InvalidTemplateException.class, exception -> {
                    assertThat(exception.getMessage()).contains("Missing row at index 3");
                });
    }
}
