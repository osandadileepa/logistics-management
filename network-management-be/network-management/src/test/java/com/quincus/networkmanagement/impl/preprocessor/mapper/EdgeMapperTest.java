package com.quincus.networkmanagement.impl.preprocessor.mapper;

import com.quincus.networkmanagement.api.domain.Connection;
import com.quincus.networkmanagement.impl.preprocessor.model.Edge;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Spy;

import static com.quincus.networkmanagement.impl.data.NetworkManagementTestData.dummyGroundConnection;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class EdgeMapperTest {
    @Spy
    private EdgeMapper edgeMapper = Mappers.getMapper(EdgeMapper.class);

    @Test
    @DisplayName("GIVEN connection WHEN mapToEdge THEN return expected")
    void returnExpectedWhenMapToGraphNode() {
        Connection connection = dummyGroundConnection();

        Edge edge = edgeMapper.toEdge(connection);

        assertThat(edge).isNotNull().isInstanceOf(Edge.class);

        assertThat(edge.getFlightNumber()).isEqualTo(connection.getConnectionCode());
        assertThat(edge.getDepartureHub()).isEqualTo(connection.getDepartureNode().getNodeCode());
        assertThat(edge.getDepartureLat()).isEqualTo(connection.getDepartureNode().getFacility().getLat().doubleValue());
        assertThat(edge.getDepartureLon()).isEqualTo(connection.getDepartureNode().getFacility().getLon().doubleValue());
        assertThat(edge.getArrivalHub()).isEqualTo(connection.getArrivalNode().getNodeCode());
        assertThat(edge.getArrivalLat()).isEqualTo(connection.getArrivalNode().getFacility().getLat().doubleValue());
        assertThat(edge.getArrivalLon()).isEqualTo(connection.getArrivalNode().getFacility().getLon().doubleValue());
        assertThat(edge.getVehicleType()).isEqualTo(connection.getVehicleType().getName());
    }
}

