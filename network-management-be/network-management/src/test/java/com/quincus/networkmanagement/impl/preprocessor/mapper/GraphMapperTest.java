package com.quincus.networkmanagement.impl.preprocessor.mapper;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.quincus.networkmanagement.api.domain.Node;
import com.quincus.networkmanagement.impl.preprocessor.calculator.GraphNodeCalculator;
import com.quincus.networkmanagement.impl.preprocessor.calculator.impl.GraphNodeCalculatorImpl;
import com.quincus.networkmanagement.impl.preprocessor.mapper.impl.GraphMapperImpl;
import com.quincus.networkmanagement.impl.preprocessor.model.Edge;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.quincus.networkmanagement.impl.data.NetworkManagementTestData.dummyEdge;
import static com.quincus.networkmanagement.impl.data.NetworkManagementTestData.dummyNode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class GraphMapperTest {

    GraphNodeCalculator graphNodeCalculator = new GraphNodeCalculatorImpl();
    GraphMapper graphMapper = new GraphMapperImpl(graphNodeCalculator);

    @Test
    @DisplayName("GIVEN node WHEN map domain to training input THEN return expected JSON")
    void returnExpectedWhenMapToGraphNode() {
        Node node = dummyNode();

        ArrayNode result = graphMapper.toGraphNode(node, List.of());

        assertThat(result.get(0).asText()).isEqualTo(node.getNodeCode());
        assertThat(result.get(1).get("lat").asDouble()).isEqualTo(node.getFacility().getLat().doubleValue());
        assertThat(result.get(1).get("lon").asDouble()).isEqualTo(node.getFacility().getLon().doubleValue());
        assertThat(result.get(1).get("degree").asLong()).isZero();
        assertThat(result.get(1).get("number_of_neighbors").asLong()).isZero();
    }

    @Test
    void mapToGraphEdge() {
        Edge edge = dummyEdge();

        ArrayNode result = graphMapper.toGraphEdge(edge);

        assertThat(result.get(0).asText()).isEqualTo(edge.getDepartureHub());
        assertThat(result.get(1).asText()).isEqualTo(edge.getArrivalHub());
        assertThat(result.get(2).get("flight_number").asText()).isEqualTo(edge.getFlightNumber());
        assertThat(result.get(2).get("departure_hub").asText()).isEqualTo(edge.getDepartureHub());
        assertThat(result.get(2).get("departure_lat").asDouble()).isEqualTo(edge.getDepartureLat());
        assertThat(result.get(2).get("departure_lon").asDouble()).isEqualTo(edge.getDepartureLon());
        assertThat(result.get(2).get("departure_time").asLong()).isEqualTo(edge.getDepartureTime());
        assertThat(result.get(2).get("arrival_hub").asText()).isEqualTo(edge.getArrivalHub());
        assertThat(result.get(2).get("arrival_lat").asDouble()).isEqualTo(edge.getArrivalLat());
        assertThat(result.get(2).get("arrival_lon").asDouble()).isEqualTo(edge.getArrivalLon());
        assertThat(result.get(2).get("arrival_time").asLong()).isEqualTo(edge.getArrivalTime());
    }

}
