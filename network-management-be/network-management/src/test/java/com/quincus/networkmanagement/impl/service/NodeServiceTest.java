package com.quincus.networkmanagement.impl.service;

import com.quincus.networkmanagement.api.constant.NodeType;
import com.quincus.networkmanagement.api.domain.Facility;
import com.quincus.networkmanagement.api.domain.Node;
import com.quincus.networkmanagement.api.exception.DuplicateNodeCodeException;
import com.quincus.networkmanagement.api.exception.NodeNotFoundException;
import com.quincus.networkmanagement.api.exception.QPortalSyncFailedException;
import com.quincus.networkmanagement.api.filter.NodeSearchFilter;
import com.quincus.networkmanagement.impl.mapper.NodeMapper;
import com.quincus.networkmanagement.impl.repository.NodeRepository;
import com.quincus.networkmanagement.impl.repository.entity.NodeEntity;
import com.quincus.networkmanagement.impl.repository.specification.NodeSpecification;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.quincus.networkmanagement.impl.data.NetworkManagementTestData.dummyNode;
import static com.quincus.networkmanagement.impl.data.NetworkManagementTestData.dummyNodeEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {NodeService.class})
class NodeServiceTest {
    @Mock
    private NodeRepository nodeRepository;
    @Spy
    private NodeMapper nodeMapper = Mappers.getMapper(NodeMapper.class);
    @Mock
    private QPortalService qPortalService;
    @InjectMocks
    private NodeService nodeService;

    @Test
    void testGetAllActiveNodesEmpty() {
        String organizationId = "ORGANIZATION-ID";
        when(nodeRepository.findAllActiveByOrganizationId(organizationId)).thenReturn(new ArrayList<>());
        assertTrue(nodeService.getAllActiveNodesByOrganization(organizationId).isEmpty());
        verify(nodeRepository).findAllActiveByOrganizationId(organizationId);
    }

    @Test
    @Disabled("TODO: Complete this test")
    void testGetAllActiveNodesWithResults() {
        String organizationId = "ORGANIZATION-ID";
        Node node = dummyNode();
        List<NodeEntity> nodeEntityList = List.of(dummyNodeEntity(node));

        when(nodeRepository.findAllActiveByOrganizationId(organizationId)).thenReturn(nodeEntityList);
        when(nodeMapper.toDomain(Mockito.any())).thenReturn(node);

        List<Node> allActiveNodes = nodeService.getAllActiveNodesByOrganization(any());

        assertThat(allActiveNodes).isNotNull().hasSize(nodeEntityList.size());
        verify(nodeRepository).findAllActiveByOrganizationId(organizationId);
    }

    @Test
    void createNodeSuccessWithValidFacilityTest() {
        Node node = dummyNode();

        when(nodeRepository.isExistingNodeCode(any())).thenReturn(false);
        when(nodeRepository.save(any()))
                .thenAnswer(i -> i.getArguments()[0]);
        doNothing().when(qPortalService).validateTags(any());
        when(qPortalService.syncPartner(node.getVendor())).thenReturn(node.getVendor());
        when(qPortalService.syncFacility(node.getFacility())).thenReturn(node.getFacility());

        Node result = nodeService.create(node);

        assertThat(result).isNotNull();
        assertThat(result.getNodeCode()).isEqualTo(node.getNodeCode());
        assertThat(result.getFacility()).isNotNull();
        assertThat(result.getVendor()).isNotNull();
        assertThat(result.getFacility().getLat()).isNotNull();
        assertThat(result.getFacility().getLon()).isNotNull();
        assertThat(result.getTimezone()).isNotNull();

        verify(nodeRepository, times(1)).isExistingNodeCode(any());
    }

    @Test
    void createNodeWithInvalidFacilityTest() {
        Node node = dummyNode();

        when(nodeRepository.isExistingNodeCode(any())).thenReturn(false);
        doNothing().when(qPortalService).validateTags(any());
        when(qPortalService.syncFacility(node.getFacility())).thenThrow(QPortalSyncFailedException.class);

        assertThrows(QPortalSyncFailedException.class, () -> nodeService.create(node));

        verify(nodeRepository).isExistingNodeCode(any());
    }

    @Test
    void createNodeWithDuplicateCodeTest() {
        Node node = dummyNode();

        when(nodeRepository.isExistingNodeCode(any())).thenReturn(true);

        assertThrows(DuplicateNodeCodeException.class, () -> nodeService.create(node));

        verify(nodeRepository).isExistingNodeCode(any());
    }

    @Test
    void updateNodeWithValidFacility() {
        Node node = dummyNode();
        NodeEntity nodeEntity = dummyNodeEntity(node);

        when(nodeRepository.findById(any())).thenReturn(Optional.of(nodeEntity));
        doNothing().when(qPortalService).validateTags(any());
        when(qPortalService.syncPartner(node.getVendor())).thenReturn(node.getVendor());
        when(qPortalService.syncFacility(node.getFacility())).thenReturn(node.getFacility());
        when(nodeRepository.save(any())).thenReturn(nodeEntity);

        Node result = nodeService.update(node);

        assertThat(result).isNotNull();

        verify(nodeRepository).findById(any());
        verify(qPortalService).validateTags(any());
        verify(nodeRepository).save(any());
    }

    @Test
    void updateNodeWithInvalidFacility() {
        Node node = dummyNode();
        NodeEntity nodeEntity = dummyNodeEntity(node);

        when(nodeRepository.findById(any())).thenReturn(Optional.of(nodeEntity));
        doNothing().when(qPortalService).validateTags(any());
        when(qPortalService.syncFacility(node.getFacility())).thenThrow(QPortalSyncFailedException.class);

        assertThrows(QPortalSyncFailedException.class, () -> nodeService.update(node));

        verify(nodeRepository).findById(any());
        verify(qPortalService).validateTags(any());
    }

    @Test
    void updateWithNonExistingNodeTest() {
        Node node = new Node();
        node.setId("test-id");
        node.setNodeCode("test-code");
        when(nodeRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(NodeNotFoundException.class, () -> nodeService.update(node));

        verify(nodeRepository).findById(any());
    }

    @Test
    void nodeListingTest() {
        NodeSearchFilter filter = new NodeSearchFilter();
        filter.setActive(true);
        filter.setFacilityId("112-221j2j2-2jj2");
        filter.setNodeCode("Node Code");
        filter.setNodeType(NodeType.BAGGAGE_CLAIM);
        filter.setTags(new String[]{"Tags"});
        filter.setVendorId("233jwnwgwg");

        when(nodeRepository.findAll(Mockito.<Specification<NodeEntity>>any(), Mockito.<Pageable>any()))
                .thenReturn(new PageImpl<>(new ArrayList<>()));

        assertTrue(nodeService.list(filter, null).toList().isEmpty());

        verify(nodeRepository).findAll(Mockito.<Specification<NodeEntity>>any(), Mockito.<Pageable>any());
    }

    @Test
    void createWithNoRollbackTest() {
        Node node = dummyNode();

        when(nodeRepository.isExistingNodeCode(any())).thenReturn(false);
        when(nodeMapper.toEntity(any())).thenThrow(new NodeNotFoundException("An error occurred"));
        when(qPortalService.syncPartner(any())).thenReturn(node.getVendor());
        when(qPortalService.syncFacility(any())).thenReturn(new Facility());
        doNothing().when(qPortalService).validateTags(any());

        assertThrows(NodeNotFoundException.class, () -> nodeService.createWithNoRollback(node));

        verify(nodeRepository).isExistingNodeCode(any());
        verify(nodeMapper).toEntity(any());
        verify(qPortalService).syncFacility(any());
        verify(qPortalService).syncPartner(any());
        verify(qPortalService).validateTags(any());
    }

    @Test
    void updateNodeCode() {
        Node node = dummyNode();
        NodeEntity nodeEntity = dummyNodeEntity(node);

        node.setNodeCode(UUID.randomUUID().toString());

        when(nodeRepository.findById(any())).thenReturn(Optional.of(nodeEntity));
        when(nodeRepository.isExistingNodeCode(any())).thenReturn(false);
        doNothing().when(qPortalService).validateTags(any());
        when(qPortalService.syncPartner(node.getVendor())).thenReturn(node.getVendor());
        when(qPortalService.syncFacility(node.getFacility())).thenReturn(node.getFacility());
        when(nodeRepository.save(any())).thenReturn(nodeEntity);

        Node result = nodeService.update(node);

        assertThat(result).isNotNull();

        verify(nodeRepository).findById(any());
        verify(nodeRepository).isExistingNodeCode(any());
        verify(qPortalService).validateTags(any());
        verify(nodeRepository).save(any());
    }

    @Test
    void testDelete() {
        String nodeId = "nodeId";
        NodeEntity mockNodeEntity = new NodeEntity();
        mockNodeEntity.setNodeCode("nodeCode");

        when(nodeRepository.findById(nodeId)).thenReturn(Optional.of(mockNodeEntity));

        nodeService.delete(nodeId);

        assertThat(mockNodeEntity.isDeleted()).isTrue();

        verify(nodeRepository).save(mockNodeEntity);
    }

    @Test
    void testNodeList() {
        NodeSearchFilter mockFilter = new NodeSearchFilter();
        Pageable mockPageable = PageRequest.of(0, 10);
        List<NodeEntity> mockNodeEntities = new ArrayList<>();
        mockNodeEntities.add(new NodeEntity());
        mockNodeEntities.add(new NodeEntity());
        Page<NodeEntity> mockPage = new PageImpl<>(mockNodeEntities);

        when(nodeRepository.findAll(any(NodeSpecification.class), eq(mockPageable))).thenReturn(mockPage);
        when(nodeMapper.toSearchResult(any(NodeEntity.class))).thenReturn(new Node());

        Page<Node> result = nodeService.list(mockFilter, mockPageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(mockNodeEntities.size());

        verify(nodeRepository).findAll(any(NodeSpecification.class), eq(mockPageable));
        verify(nodeMapper, times(mockNodeEntities.size())).toSearchResult(any(NodeEntity.class));
    }
}

