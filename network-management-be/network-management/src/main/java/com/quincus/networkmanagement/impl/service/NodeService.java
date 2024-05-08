package com.quincus.networkmanagement.impl.service;

import com.quincus.networkmanagement.api.domain.Node;
import com.quincus.networkmanagement.api.exception.DuplicateNodeCodeException;
import com.quincus.networkmanagement.api.exception.NodeNotFoundException;
import com.quincus.networkmanagement.api.filter.NodeSearchFilter;
import com.quincus.networkmanagement.impl.annotation.NetworkChange;
import com.quincus.networkmanagement.impl.mapper.NodeMapper;
import com.quincus.networkmanagement.impl.repository.NodeRepository;
import com.quincus.networkmanagement.impl.repository.entity.NodeEntity;
import com.quincus.networkmanagement.impl.repository.specification.NodeSpecification;
import com.quincus.web.common.exception.model.QuincusException;
import com.quincus.web.common.exception.model.QuincusValidationException;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class NodeService {

    private static final String DELETED_SUFFIX_SEPARATOR = "-";

    private final NodeRepository nodeRepository;

    private final NodeMapper nodeMapper;

    private final QPortalService qPortalService;

    public List<Node> getAllActiveNodesByOrganization(String organizationId) {
        return nodeRepository.findAllActiveByOrganizationId(organizationId).stream().map(nodeMapper::toDomain).toList();
    }

    public Node find(String id) {
        return nodeMapper.toDomain(findByIdOrThrow(id));
    }

    private NodeEntity findByIdOrThrow(String id) {
        return nodeRepository.findById(id)
                .orElseThrow(() -> new NodeNotFoundException(String.format("Node record with id `%s` not found", id)));
    }

    @Transactional
    @NetworkChange
    public Node create(Node node) {
        node.setId(null);
        validateNodeCode(node);
        syncQPortalEntities(node);
        return nodeMapper.toDomain(nodeRepository.save(nodeMapper.toEntity(node)));
    }


    @Transactional
    @NetworkChange
    public Node update(Node node) {
        NodeEntity nodeEntity = findByIdOrThrow(node.getId());
        if (!StringUtils.equals(node.getNodeCode(), nodeEntity.getNodeCode())) {
            validateNodeCode(node);
        }
        syncQPortalEntities(node);
        return nodeMapper.toDomain(nodeRepository.save(nodeMapper.update(node, nodeEntity)));
    }


    private void validateNodeCode(Node node) {
        if (nodeRepository.isExistingNodeCode(node.getNodeCode())) {
            throw new DuplicateNodeCodeException(node.getNodeCode());
        }
    }

    private void syncQPortalEntities(Node node) {
        qPortalService.validateTags(node.getTags());
        Optional.of(qPortalService.syncFacility(node.getFacility())).ifPresent(
                facility -> {
                    node.setFacility(facility);
                    node.setTimezone(facility.getTimezone());
                }
        );
        node.setVendor(qPortalService.syncPartner(node.getVendor()));
    }

    @Transactional
    @NetworkChange
    public void delete(String id) {
        NodeEntity node = findByIdOrThrow(id);
        node.setDeleted(true);
        node.setNodeCode(node.getNodeCode() + DELETED_SUFFIX_SEPARATOR + System.currentTimeMillis());
        nodeRepository.save(node);
    }

    public Page<Node> list(NodeSearchFilter filter, Pageable pageable) {
        NodeSpecification specification = new NodeSpecification(filter);
        return nodeRepository.findAll(specification, pageable).map(nodeMapper::toSearchResult);
    }

    /**
     * These methods are used for Async services to not roll back any data when expected exception occurs
     */
    @Transactional(noRollbackFor = {QuincusValidationException.class, QuincusException.class})
    @NetworkChange
    public void createOrUpdateWithNoRollback(Node node) {
        nodeRepository.findByNodeCode(node.getNodeCode()).ifPresentOrElse(
                connectionEntity -> {
                    node.setId(connectionEntity.getId());
                    update(node);
                },
                () -> create(node));
    }

    @Transactional(noRollbackFor = {QuincusValidationException.class, QuincusException.class})
    @NetworkChange
    public void createWithNoRollback(Node node) {
        create(node);
    }

}
