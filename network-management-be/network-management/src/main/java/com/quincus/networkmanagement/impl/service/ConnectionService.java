package com.quincus.networkmanagement.impl.service;

import com.quincus.networkmanagement.api.constant.DistanceUnit;
import com.quincus.networkmanagement.api.constant.TransportType;
import com.quincus.networkmanagement.api.domain.Connection;
import com.quincus.networkmanagement.api.domain.Node;
import com.quincus.networkmanagement.api.exception.ConnectionNotFoundException;
import com.quincus.networkmanagement.api.exception.DuplicateConnectionCodeException;
import com.quincus.networkmanagement.api.exception.InvalidConnectionException;
import com.quincus.networkmanagement.api.exception.NodeNotFoundException;
import com.quincus.networkmanagement.api.filter.ConnectionSearchFilter;
import com.quincus.networkmanagement.impl.annotation.NetworkChange;
import com.quincus.networkmanagement.impl.mapper.ConnectionMapper;
import com.quincus.networkmanagement.impl.mapper.NodeMapper;
import com.quincus.networkmanagement.impl.repository.ConnectionRepository;
import com.quincus.networkmanagement.impl.repository.NodeRepository;
import com.quincus.networkmanagement.impl.repository.entity.ConnectionEntity;
import com.quincus.networkmanagement.impl.repository.entity.NodeEntity;
import com.quincus.networkmanagement.impl.repository.specification.ConnectionSpecification;
import com.quincus.networkmanagement.impl.service.calculator.DistanceCalculator;
import com.quincus.web.common.exception.model.QuincusException;
import com.quincus.web.common.exception.model.QuincusValidationException;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ConnectionService {
    private static final String DELETED_SUFFIX_SEPARATOR = "-";
    private static final DistanceUnit DEFAULT_DISTANCE_UNIT = DistanceUnit.KILOMETERS;
    private final ConnectionRepository connectionRepository;

    private final ConnectionMapper connectionMapper;

    private final QPortalService qPortalService;

    private final NodeRepository nodeRepository;

    private final NodeMapper nodeMapper;

    private final DistanceCalculator distanceCalculator;

    public List<Connection> getAllActiveConnectionsByOrganization(String organizationId) {
        return connectionRepository.findAllActiveByOrganizationId(organizationId).stream().map(connectionMapper::toDomain).toList();
    }

    public Connection find(String id) {
        return connectionMapper.toDomain(findByIdOrThrow(id));
    }

    private ConnectionEntity findByIdOrThrow(String id) {
        return connectionRepository.findById(id)
                .orElseThrow(() -> new ConnectionNotFoundException(String.format("Connection record with id `%s` not found", id)));
    }

    @Transactional
    @NetworkChange
    public Connection create(Connection connection) {
        connection.setId(null);
        validateConnection(connection);
        syncNodes(connection);
        syncQPortalEntities(connection);
        ConnectionEntity connectionEntity = connectionMapper.toEntity(connection);
        return connectionMapper.toDomain(connectionRepository.save(connectionEntity));
    }

    @Transactional
    @NetworkChange
    public Connection update(Connection connection) {
        ConnectionEntity connectionEntity = findByIdOrThrow(connection.getId());
        if (!StringUtils.equals(connectionEntity.getConnectionCode(), connection.getConnectionCode())) {
            validateConnection(connection);
        }
        syncNodes(connection);
        syncQPortalEntities(connection);
        return connectionMapper.toDomain(connectionRepository.save(connectionMapper.update(connection, connectionEntity)));
    }

    private void validateConnection(Connection connection) {
        if (connectionRepository.isExistingConnectionCode(connection.getConnectionCode())) {
            throw new DuplicateConnectionCodeException(connection.getConnectionCode());
        }
    }

    private void syncNodes(Connection connection) {

        Node departureNode = syncNode(connection.getDepartureNode());
        Node arrivalNode = syncNode(connection.getArrivalNode());

        if (departureNode.getId().equals(arrivalNode.getId())) {
            throw new InvalidConnectionException("Departure node and arrival node cannot be the same");
        }
        connection.setDepartureNode(departureNode);
        connection.setArrivalNode(arrivalNode);
        calculateDistance(connection, departureNode, arrivalNode);
    }

    private Node syncNode(Node node) {
        if (StringUtils.isNotBlank(node.getNodeCode())) {
            Optional<NodeEntity> optionalNode = nodeRepository.findByNodeCode(node.getNodeCode());

            if (optionalNode.isPresent()) {
                return nodeMapper.toConnectionNode(optionalNode.get());
            }
            throw new NodeNotFoundException(String.format("Node record with code `%s` not found", node.getNodeCode()));
        }

        Optional<NodeEntity> optionalNode = nodeRepository.findById(node.getId());
        if (optionalNode.isPresent()) {
            return nodeMapper.toConnectionNode(optionalNode.get());
        }
        throw new NodeNotFoundException(String.format("Node record with id `%s` not found", node.getId()));
    }

    private void calculateDistance(Connection connection, Node departureNode, Node arrivalNode) {
        BigDecimal distance = distanceCalculator.calculateDistance(departureNode, arrivalNode, DEFAULT_DISTANCE_UNIT);
        connection.setDistance(distance);
        connection.getMeasurementUnits().setDistanceUnit(DEFAULT_DISTANCE_UNIT);
    }

    private void syncQPortalEntities(Connection connection) {
        qPortalService.validateTags(connection.getTags());
        connection.setVendor(qPortalService.syncPartner(connection.getVendor()));
        if (connection.getTransportType() == TransportType.GROUND) {
            connection.setVehicleType(qPortalService.syncVehicleType(connection.getVehicleType()));
        }
        connection.setCurrency(qPortalService.syncCurrency(connection.getCurrency()));
    }

    @Transactional
    @NetworkChange
    public void delete(String id) {
        ConnectionEntity connection = findByIdOrThrow(id);
        connection.setDeleted(true);
        connection.setConnectionCode(connection.getConnectionCode() + DELETED_SUFFIX_SEPARATOR + System.currentTimeMillis());
        connectionRepository.save(connection);
    }

    public Page<Connection> list(ConnectionSearchFilter filter, Pageable pageable) {
        ConnectionSpecification specification = new ConnectionSpecification(filter);
        return connectionRepository.findAll(specification, pageable).map(connectionMapper::toSearchResult);
    }

    /**
     * These methods are used for Async services to not roll back any data when expected exception occurs
     */
    @Transactional(noRollbackFor = {QuincusValidationException.class, QuincusException.class})
    @NetworkChange
    public void createOrUpdateWithNoRollback(Connection connection) {
        connectionRepository.findByConnectionCode(connection.getConnectionCode()).ifPresentOrElse(
                connectionEntity -> {
                    connection.setId(connectionEntity.getId());
                    update(connection);
                },
                () -> create(connection));
    }

    @Transactional(noRollbackFor = {QuincusValidationException.class, QuincusException.class})
    @NetworkChange
    public void createWithNoRollback(Connection connection) {
        create(connection);
    }

}
