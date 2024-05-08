package com.quincus.networkmanagement.impl.mapper;

import com.quincus.networkmanagement.api.constant.NodeType;
import com.quincus.networkmanagement.api.domain.Node;
import com.quincus.networkmanagement.impl.repository.entity.NodeEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Spy;

import java.util.List;
import java.util.UUID;

import static com.quincus.networkmanagement.impl.data.NetworkManagementTestData.dummyNode;
import static com.quincus.networkmanagement.impl.data.NetworkManagementTestData.dummyNodeEntity;
import static org.assertj.core.api.Assertions.assertThat;

class NodeMapperTest {
    @Spy
    private NodeMapper nodeMapper = Mappers.getMapper(NodeMapper.class);

    @Test
    void testToDomain() {
        NodeEntity entity = new NodeEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setNodeCode(UUID.randomUUID().toString());
        entity.setDeleted(false);
        entity.setNodeType(NodeType.CARGO);
        entity.setDescription("Description");
        entity.setActive(true);
        entity.setTags(List.of("tag 1", "tag 2"));
        entity.setAddressLine1("line1");
        entity.setAddressLine2("line2");
        entity.setAddressLine3("line3");
        entity.setTimezone("Asia/Manila");

        Node domain = nodeMapper.toDomain(entity);

        assertThat(domain).isNotNull();
        assertThat(domain)
                .usingRecursiveComparison()
                .isEqualTo(entity);
    }

    @Test
    void testToEntity() {
        Node domain = new Node();
        domain.setId(UUID.randomUUID().toString());
        domain.setNodeCode(UUID.randomUUID().toString());
        domain.setDeleted(false);
        domain.setNodeType(NodeType.CARGO);
        domain.setDescription("Description");
        domain.setActive(true);
        domain.setTags(List.of("tag 1", "tag 2"));
        domain.setAddressLine1("line1");
        domain.setAddressLine2("line2");
        domain.setAddressLine3("line3");
        domain.setTimezone("Asia/Manila");

        NodeEntity entity = nodeMapper.toEntity(domain);

        assertThat(entity).isNotNull();
        assertThat(entity)
                .usingRecursiveComparison()
                .ignoringFields("version", "modifyTime", "createTime")
                .isEqualTo(domain);
    }


    @Test
    void testUpdate() {
        Node node = dummyNode();
        NodeEntity nodeEntity = dummyNodeEntity(node);

        NodeEntity result = nodeMapper.update(node, nodeEntity);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(node.getId());
        assertThat(result.getNodeCode()).isEqualTo(node.getNodeCode());
    }

    @Test
    void testToSearchResult() {
        NodeEntity entity = new NodeEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setNodeCode(UUID.randomUUID().toString());
        entity.setOrganizationId(UUID.randomUUID().toString());
        entity.setNodeType(NodeType.CARGO);
        entity.setActive(true);
        entity.setTags(List.of("tag 1", "tag 2"));

        Node searchResult = nodeMapper.toSearchResult(entity);

        assertThat(searchResult).isNotNull();
        assertThat(searchResult)
                .usingRecursiveComparison()
                .isEqualTo(entity);

    }

    @Test
    void testToConnectionNode() {
        NodeEntity entity = new NodeEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setNodeCode(UUID.randomUUID().toString());

        Node connectionNode = nodeMapper.toConnectionNode(entity);

        assertThat(connectionNode).isNotNull();
        assertThat(connectionNode)
                .usingRecursiveComparison()
                .ignoringFields("active")
                .isEqualTo(entity);
    }

}
