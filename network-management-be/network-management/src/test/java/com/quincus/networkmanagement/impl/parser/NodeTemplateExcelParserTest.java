package com.quincus.networkmanagement.impl.parser;

import com.quincus.networkmanagement.api.exception.InvalidTemplateException;
import com.quincus.networkmanagement.impl.attachment.node.NodeRecord;
import com.quincus.networkmanagement.impl.attachment.node.parser.NodeTemplateExcelParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.quincus.networkmanagement.impl.data.NetworkManagementRecordTestData.dummyNodeRecord;
import static com.quincus.networkmanagement.impl.data.NetworkManagementRecordTestData.dummyNodesExcelTemplate;
import static com.quincus.networkmanagement.impl.data.NetworkManagementRecordTestData.dummyTemplate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class NodeTemplateExcelParserTest {

    NodeTemplateExcelParser parser = new NodeTemplateExcelParser();

    @Test
    @DisplayName("GIVEN valid template WHEN parse XLS file THEN return expected")
    void returnExpectedWhenValidExcelTemplate() {
        MultipartFile template = dummyNodesExcelTemplate();
        List<NodeRecord> result = parser.parseFile(template);

        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(List.of(dummyNodeRecord()));
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
