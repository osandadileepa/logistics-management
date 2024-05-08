package com.quincus.networkmanagement.impl.mapper;

import com.quincus.networkmanagement.api.domain.Node;
import com.quincus.networkmanagement.impl.attachment.node.NodeRecord;
import com.quincus.networkmanagement.impl.attachment.node.NodeRecordMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Spy;

import static com.quincus.networkmanagement.impl.data.NetworkManagementRecordTestData.dummyNodeRecord;
import static com.quincus.networkmanagement.impl.data.NetworkManagementTestData.dummyNode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;

class NodeRecordMapperTest {
    @Spy
    private NodeRecordMapper mapper = Mappers.getMapper(NodeRecordMapper.class);

    @Test
    @DisplayName("given nodeRecord when map record to domain then return expected node")
    void returnExpectedWhenMapNodeRecordToDomain() {
        NodeRecord record = dummyNodeRecord();
        assertThatNoException().isThrownBy(() -> mapper.toDomain(record));
    }

    @Test
    @DisplayName("given node when map domain to record then return expected node record")
    void returnExpectedWhenMapNodeToRecord() {
        Node domain = dummyNode();
        assertThatNoException().isThrownBy(() -> mapper.toRecord(domain));
    }

}
