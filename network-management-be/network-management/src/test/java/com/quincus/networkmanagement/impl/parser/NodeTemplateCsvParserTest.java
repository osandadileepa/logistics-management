package com.quincus.networkmanagement.impl.parser;

import com.quincus.networkmanagement.impl.attachment.node.NodeRecord;
import com.quincus.networkmanagement.impl.attachment.node.parser.NodeTemplateCSVParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.quincus.networkmanagement.impl.data.NetworkManagementRecordTestData.dummyNodeRecord;
import static com.quincus.networkmanagement.impl.data.NetworkManagementRecordTestData.dummyNodesCsvTemplate;
import static org.assertj.core.api.Assertions.assertThat;

class NodeTemplateCsvParserTest {

    NodeTemplateCSVParser parser = new NodeTemplateCSVParser();

    @Test
    @DisplayName("GIVEN valid template WHEN parse XLS file THEN return expected")
    void returnExpectedWhenValidExcelTemplate() {
        MultipartFile template = dummyNodesCsvTemplate();
        List<NodeRecord> result = parser.parseFile(template);

        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(List.of(dummyNodeRecord()));
    }
}
