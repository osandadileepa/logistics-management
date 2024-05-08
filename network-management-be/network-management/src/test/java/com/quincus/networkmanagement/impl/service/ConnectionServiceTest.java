package com.quincus.networkmanagement.impl.service;

import com.quincus.networkmanagement.api.domain.Connection;
import com.quincus.networkmanagement.api.domain.Node;
import com.quincus.networkmanagement.api.domain.VehicleType;
import com.quincus.networkmanagement.api.exception.DuplicateConnectionCodeException;
import com.quincus.networkmanagement.api.exception.InvalidConnectionException;
import com.quincus.networkmanagement.api.exception.NodeNotFoundException;
import com.quincus.networkmanagement.api.filter.ConnectionSearchFilter;
import com.quincus.networkmanagement.impl.data.NetworkManagementTestData;
import com.quincus.networkmanagement.impl.mapper.ConnectionMapper;
import com.quincus.networkmanagement.impl.mapper.NodeMapper;
import com.quincus.networkmanagement.impl.repository.ConnectionRepository;
import com.quincus.networkmanagement.impl.repository.NodeRepository;
import com.quincus.networkmanagement.impl.repository.entity.ConnectionEntity;
import com.quincus.networkmanagement.impl.repository.specification.ConnectionSpecification;
import com.quincus.networkmanagement.impl.service.calculator.DistanceCalculator;
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
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.quincus.networkmanagement.impl.data.NetworkManagementTestData.dummyAirConnection;
import static com.quincus.networkmanagement.impl.data.NetworkManagementTestData.dummyConnectionEntity;
import static com.quincus.networkmanagement.impl.data.NetworkManagementTestData.dummyGroundConnection;
import static com.quincus.networkmanagement.impl.data.NetworkManagementTestData.dummyNodeEntity;
import static com.quincus.networkmanagement.impl.data.NetworkManagementTestData.dummyVehicleType;
import static com.quincus.networkmanagement.impl.mapper.ConnectionMapper.AIR_CONNECTION_VEHICLE_TYPE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {ConnectionService.class})
class ConnectionServiceTest {

    @Mock
    private ConnectionRepository connectionRepository;
    @Mock
    private QPortalService qPortalService;
    @Mock
    private NodeRepository nodeRepository;
    @Spy
    private ConnectionMapper connectionMapper = Mappers.getMapper(ConnectionMapper.class);
    @Spy
    private NodeMapper nodeMapper = Mappers.getMapper(NodeMapper.class);
    @Mock
    private DistanceCalculator distanceCalculator;
    @InjectMocks
    private ConnectionService connectionService;

    @Test
    void testGetAllActiveConnectionsEmpty() {
        String organizationId = "ORGANIZATION-ID";
        when(connectionRepository.findAllActiveByOrganizationId(organizationId)).thenReturn(new ArrayList<>());
        assertTrue(connectionService.getAllActiveConnectionsByOrganization(organizationId).isEmpty());
        verify(connectionRepository).findAllActiveByOrganizationId(organizationId);
    }

    @Test
    void getAllActiveConnectionsWithResults() {
        String organizationId = "ORGANIZATION-ID";
        Connection connection = dummyAirConnection();
        List<ConnectionEntity> connectionEntityList = List.of(NetworkManagementTestData.dummyConnectionEntity(connection));

        when(connectionRepository.findAllActiveByOrganizationId(organizationId)).thenReturn(connectionEntityList);
        when(connectionMapper.toDomain(Mockito.any())).thenReturn(connection);
        List<Connection> allActiveConnections = connectionService.getAllActiveConnectionsByOrganization(organizationId);

        assertThat(allActiveConnections).isNotNull().hasSize(connectionEntityList.size());
        verify(connectionRepository).findAllActiveByOrganizationId(organizationId);
    }

    @Test
    void createAirConnectionSuccessWithDistance() {
        Connection connection = dummyAirConnection();
        BigDecimal distance = new BigDecimal("344.321");

        when(connectionRepository.isExistingConnectionCode(any()))
                .thenReturn(false);
        when(nodeRepository.findByNodeCode(connection.getDepartureNode().getNodeCode()))
                .thenReturn(Optional.of(dummyNodeEntity(connection.getDepartureNode())));
        when(nodeRepository.findByNodeCode(connection.getArrivalNode().getNodeCode()))
                .thenReturn(Optional.of(dummyNodeEntity(connection.getArrivalNode())));
        when(connectionRepository.save(any(ConnectionEntity.class)))
                .thenAnswer(i -> i.getArguments()[0]);
        when(distanceCalculator.calculateDistance(any(Node.class), any(Node.class), any()))
                .thenReturn(distance);

        Connection result = connectionService.create(connection);

        assertThat(result).isNotNull();
        assertThat(result.getConnectionCode()).isEqualTo(connection.getConnectionCode());
        assertThat(result.getDistance()).isNotNull();
        assertThat(result.getDistance()).isEqualTo(distance);

        verify(connectionRepository, times(1)).isExistingConnectionCode(any());
        verify(connectionRepository, times(1)).save(any());
        verify(qPortalService, times(1)).validateTags(any());
        verify(qPortalService, times(1)).syncPartner(any());
        verify(qPortalService, times(1)).syncCurrency(any());
        verify(nodeRepository, times(2)).findByNodeCode(any());
        verify(distanceCalculator, times(1)).calculateDistance(any(Node.class), any(Node.class), any());
        verify(nodeMapper, times(2)).toConnectionNode(any());
        verify(connectionMapper, times(1)).toEntity(any());
        verify(connectionMapper, times(1)).toDomain(any());
    }

    @Test
    void createGroundConnectionSuccessWithDistance() {
        Connection connection = dummyGroundConnection();
        BigDecimal distance = new BigDecimal("533.3211");

        when(connectionRepository.isExistingConnectionCode(any()))
                .thenReturn(false);
        when(nodeRepository.findByNodeCode(connection.getDepartureNode().getNodeCode()))
                .thenReturn(Optional.of(dummyNodeEntity(connection.getDepartureNode())));
        when(nodeRepository.findByNodeCode(connection.getArrivalNode().getNodeCode()))
                .thenReturn(Optional.of(dummyNodeEntity(connection.getArrivalNode())));
        when(connectionRepository.save(any(ConnectionEntity.class)))
                .thenAnswer(i -> i.getArguments()[0]);
        when(distanceCalculator.calculateDistance(any(Node.class), any(Node.class), any()))
                .thenReturn(distance);

        Connection result = connectionService.create(connection);

        assertThat(result).isNotNull();
        assertThat(result.getConnectionCode()).isEqualTo(connection.getConnectionCode());
        assertThat(result.getDistance()).isNotNull();
        assertThat(result.getDistance()).isEqualTo(distance);

        verify(connectionRepository, times(1)).isExistingConnectionCode(any());
        verify(connectionRepository, times(1)).save(any());
        verify(nodeRepository, times(2)).findByNodeCode(any());
        verify(nodeMapper, times(2)).toConnectionNode(any());
        verify(qPortalService, times(1)).validateTags(any());
        verify(qPortalService, times(1)).syncPartner(any());
        verify(qPortalService, times(1)).syncCurrency(any());
        verify(qPortalService, times(1)).syncVehicleType(any());
        verify(distanceCalculator, times(1)).calculateDistance(any(Node.class), any(Node.class), any());
        verify(connectionMapper, times(1)).toEntity(any());
        verify(connectionMapper, times(1)).toDomain(any());
    }

    @Test
    void createConnectionFailTest() {
        Connection connection = dummyAirConnection();

        when(connectionRepository.isExistingConnectionCode(any())).thenReturn(true);

        assertThrows(DuplicateConnectionCodeException.class, () -> connectionService.create(connection));
        verify(connectionRepository).isExistingConnectionCode(any());
    }

    @Test
    void createConnectionFailWithMissingNodeTest() {
        Connection connection = dummyAirConnection();

        when(connectionRepository.isExistingConnectionCode(any())).thenReturn(false);
        when(nodeRepository.findByNodeCode(any())).thenReturn(Optional.empty());

        assertThrows(NodeNotFoundException.class, () -> connectionService.create(connection));
        verify(connectionRepository).isExistingConnectionCode(any());
        verify(nodeRepository, times(1)).findByNodeCode(any());
    }

    @Test
    void connectionCreateErrorWithSameNodeTests() {
        Connection connection = dummyAirConnection();
        connection.setArrivalNode(connection.getDepartureNode());

        when(connectionRepository.isExistingConnectionCode(any())).thenReturn(false);

        when(nodeRepository.findByNodeCode(connection.getDepartureNode().getNodeCode()))
                .thenReturn(Optional.of(dummyNodeEntity(connection.getDepartureNode())));

        assertThrows(InvalidConnectionException.class, () -> connectionService.create(connection));

        verify(connectionRepository).isExistingConnectionCode(any());
        verify(nodeRepository, times(2)).findByNodeCode(any());
    }


    @Test
    void airConnectionUpdateTest() {
        Connection connection = dummyAirConnection();

        when(connectionRepository.findById(any())).thenReturn(Optional.of(dummyConnectionEntity(connection)));
        when(nodeRepository.findByNodeCode(connection.getDepartureNode().getNodeCode()))
                .thenReturn(Optional.of(dummyNodeEntity(connection.getDepartureNode())));
        when(nodeRepository.findByNodeCode(connection.getArrivalNode().getNodeCode()))
                .thenReturn(Optional.of(dummyNodeEntity(connection.getArrivalNode())));
        when(connectionRepository.save(any(ConnectionEntity.class)))
                .thenAnswer(i -> i.getArguments()[0]);

        Connection result = connectionService.update(connection);

        assertThat(result).isNotNull();
        assertThat(result.getConnectionCode()).isEqualTo(connection.getConnectionCode());

        verify(connectionRepository, times(1)).findById(any());
        verify(connectionRepository, times(1)).save(any());
        verify(qPortalService, times(1)).validateTags(any());
        verify(qPortalService, times(1)).syncPartner(any());
        verify(qPortalService, times(1)).syncCurrency(any());
        verify(nodeRepository, times(2)).findByNodeCode(any());
        verify(distanceCalculator, times(1)).calculateDistance(any(Node.class), any(Node.class), any());
        verify(nodeMapper, times(2)).toConnectionNode(any());
        verify(connectionMapper, times(1)).toDomain(any());
    }

    @Test
    void createGroundConnectionWithVehicleType() {
        Connection connection = dummyGroundConnection();
        VehicleType vehicleType = dummyVehicleType();

        when(connectionRepository.isExistingConnectionCode(any()))
                .thenReturn(false);
        when(nodeRepository.findByNodeCode(connection.getDepartureNode().getNodeCode()))
                .thenReturn(Optional.of(dummyNodeEntity(connection.getDepartureNode())));
        when(nodeRepository.findByNodeCode(connection.getArrivalNode().getNodeCode()))
                .thenReturn(Optional.of(dummyNodeEntity(connection.getArrivalNode())));
        when(connectionRepository.save(any(ConnectionEntity.class)))
                .thenAnswer(i -> i.getArguments()[0]);
        when(qPortalService.syncVehicleType(any(VehicleType.class)))
                .thenReturn(vehicleType);

        Connection result = connectionService.create(connection);

        assertThat(result).isNotNull();
        assertThat(result.getConnectionCode()).isEqualTo(connection.getConnectionCode());
        assertThat(result.getVehicleType()).isNotNull();
        assertThat(result.getVehicleType().getName()).isEqualTo(vehicleType.getName());
        assertThat(result.getVehicleType().getId()).isEqualTo(vehicleType.getId());

        verify(connectionRepository, times(1)).isExistingConnectionCode(any());
        verify(connectionRepository, times(1)).save(any());
        verify(qPortalService, times(1)).validateTags(any());
        verify(qPortalService, times(1)).syncPartner(any());
        verify(qPortalService, times(1)).syncVehicleType(any());
        verify(qPortalService, times(1)).syncCurrency(any());
        verify(nodeRepository, times(2)).findByNodeCode(any());
        verify(nodeMapper, times(2)).toConnectionNode(any());
        verify(connectionMapper, times(1)).toEntity(any());
        verify(connectionMapper, times(1)).toDomain(any());
    }

    @Test
    void createAirConnectionWithoutVehicleType() {
        Connection connection = dummyAirConnection();

        when(connectionRepository.isExistingConnectionCode(any()))
                .thenReturn(false);
        when(nodeRepository.findByNodeCode(connection.getDepartureNode().getNodeCode()))
                .thenReturn(Optional.of(dummyNodeEntity(connection.getDepartureNode())));
        when(nodeRepository.findByNodeCode(connection.getArrivalNode().getNodeCode()))
                .thenReturn(Optional.of(dummyNodeEntity(connection.getArrivalNode())));
        when(connectionRepository.save(any(ConnectionEntity.class)))
                .thenAnswer(i -> i.getArguments()[0]);

        Connection result = connectionService.create(connection);

        assertThat(result).isNotNull();
        assertThat(result.getConnectionCode()).isEqualTo(connection.getConnectionCode());
        assertThat(result.getVehicleType()).isNotNull();
        assertThat(result.getVehicleType().getName()).isEqualTo(AIR_CONNECTION_VEHICLE_TYPE_NAME);

        verify(connectionRepository, times(1)).isExistingConnectionCode(any());
        verify(connectionRepository, times(1)).save(any());
        verify(qPortalService, times(1)).validateTags(any());
        verify(qPortalService, times(1)).syncPartner(any());
        verify(qPortalService, times(1)).syncCurrency(any());
        verify(nodeRepository, times(2)).findByNodeCode(any());
        verify(nodeMapper, times(2)).toConnectionNode(any());
        verify(connectionMapper, times(1)).toEntity(any());
        verify(connectionMapper, times(1)).toDomain(any());
    }


    @Test
    void testDelete() {
        String connectionId = "connectionId";
        ConnectionEntity mockConnectionEntity = new ConnectionEntity();

        when(connectionRepository.findById(connectionId)).thenReturn(Optional.of(mockConnectionEntity));

        connectionService.delete(connectionId);

        assertThat(mockConnectionEntity.isDeleted()).isTrue();

        verify(connectionRepository).findById(connectionId);
        verify(connectionRepository).save(any(ConnectionEntity.class));
    }

    @Test
    void testList() {
        ConnectionSearchFilter filter = new ConnectionSearchFilter();
        Pageable pageable = PageRequest.of(0, 10);
        Connection connection = dummyAirConnection();
        List<ConnectionEntity> mockConnectionEntities = List.of(dummyConnectionEntity(connection));
        Page<ConnectionEntity> mockPage = new PageImpl<>(mockConnectionEntities);

        when(connectionRepository.findAll(any(ConnectionSpecification.class), eq(pageable))).thenReturn(mockPage);
        when(connectionMapper.toSearchResult(any(ConnectionEntity.class))).thenReturn(connection);

        Page<Connection> result = connectionService.list(filter, pageable);

        assertThat(result).hasSize(mockConnectionEntities.size());

        verify(connectionMapper).toSearchResult(any(ConnectionEntity.class));
        verify(connectionMapper, times(mockConnectionEntities.size())).toSearchResult(any(ConnectionEntity.class));
    }

}

