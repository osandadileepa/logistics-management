package com.quincus.networkmanagement.impl.preprocessor.calculator;

import com.quincus.networkmanagement.api.domain.Connection;
import com.quincus.networkmanagement.api.domain.Node;
import com.quincus.networkmanagement.impl.preprocessor.calculator.impl.MetaDataCalculatorImpl;
import com.quincus.networkmanagement.impl.service.calculator.DistanceCalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.quincus.networkmanagement.impl.data.NetworkManagementTestData.dummyAirConnection;
import static com.quincus.networkmanagement.impl.data.NetworkManagementTestData.dummyConnection;
import static com.quincus.networkmanagement.impl.data.NetworkManagementTestData.dummyGroundConnection;
import static com.quincus.networkmanagement.impl.data.NetworkManagementTestData.dummyNode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {MetaDataCalculatorImpl.class})
class MetaDataCalculatorTest {
    @Mock
    private DistanceCalculator distanceCalculator;
    @Mock
    private GraphNodeCalculator graphNodeCalculator;
    @InjectMocks
    private MetaDataCalculatorImpl metaDataCalculator;

    @Test
    void testGetSortedCostWithData() {

        List<Double> sortedCost = metaDataCalculator.getSortedCost(
                List.of(dummyGroundConnection(), dummyAirConnection())
        );

        assertThat(sortedCost).containsExactly(3000.0, 2000.0);
    }

    @Test
    void testGetSortedDistance() {
        List<Node> nodes = getNodes();

        when(distanceCalculator.calculateDistance(any(), any(), any())).thenReturn(new BigDecimal("100"));

        Set<Double> sortedDistance = metaDataCalculator.getSortedDistance(nodes);

        assertThat(sortedDistance).containsExactly(100.0);
    }

    @Test
    void testGetNumberOfNodes() {
        List<Node> nodes = getNodes();

        Integer numberOfNodes = metaDataCalculator.getNumberOfNodes(nodes);

        assertThat(numberOfNodes).isEqualTo(3);
    }

    @Test
    void testGetBannedOrigins() {
        List<Node> nodes = getNodes();
        Node node1 = nodes.get(0);
        Node node2 = nodes.get(1);
        Node node3 = nodes.get(2);
        List<Connection> connections = List.of(dummyConnection(node2, node1), dummyConnection(node3, node1));

        List<String> bannedOrigins = metaDataCalculator.getBannedOrigins(nodes, connections);

        assertThat(bannedOrigins).containsExactly(node1.getNodeCode());
    }

    @Test
    void testGetBannedDestinations() {
        List<Node> nodes = getNodes();
        Node node1 = nodes.get(0);
        Node node2 = nodes.get(1);
        Node node3 = nodes.get(2);
        List<Connection> connections = List.of(dummyConnection(node1, node2), dummyConnection(node1, node3));

        List<String> bannedDestinations = metaDataCalculator.getBannedDestinations(nodes, connections);

        assertThat(bannedDestinations).containsExactly(node1.getNodeCode());
    }

    @Test
    void testGetMaxNeighbors() {
        List<Node> nodes = getNodes();
        List<Connection> connections = List.of(dummyConnection(nodes.get(0), nodes.get(1)));

        when(graphNodeCalculator.getNumberOfNeighbors(any(), any())).thenReturn(1L);

        Long maxNeighbors = metaDataCalculator.getMaxNeighbors(nodes, connections);

        assertThat(maxNeighbors).isEqualTo(1L);
    }

    @Test
    void testGetMaxDegree() {
        List<Node> nodes = getNodes();
        List<Connection> connections = List.of(dummyConnection(nodes.get(0), nodes.get(1)));

        when(graphNodeCalculator.getDegree(any(), any())).thenReturn(1L);

        Long maxDegree = metaDataCalculator.getMaxDegree(nodes, connections);

        assertThat(maxDegree).isEqualTo(1L);
    }

    @Test
    void testGetOrderedInDegrees() {
        List<Node> nodes = getNodes();
        Node node1 = nodes.get(0);
        Node node2 = nodes.get(1);
        Node node3 = nodes.get(2);

        List<Connection> connections = List.of(dummyConnection(node2, node1),
                dummyConnection(node1, node2),
                dummyConnection(node3, node2));

        Set<String> orderedInDegrees = metaDataCalculator.getOrderedInDegrees(connections);

        assertThat(orderedInDegrees).containsExactly(node1.getNodeCode(), node2.getNodeCode());
    }

    @Test
    void testGetOrderedOutDegrees() {
        List<Node> nodes = getNodes();
        Node node1 = nodes.get(0);
        Node node2 = nodes.get(1);
        Node node3 = nodes.get(2);

        List<Connection> connections = List.of(dummyConnection(node1, node2),
                dummyConnection(node2, node3),
                dummyConnection(node2, node1));

        Set<String> orderedOutDegrees = metaDataCalculator.getOrderedOutDegrees(connections);

        assertThat(orderedOutDegrees).containsExactly(node1.getNodeCode(), node2.getNodeCode());
    }

    @Test
    void testGetOrderedDegrees() {
        List<Node> nodes = getNodes();
        Node node1 = nodes.get(0);
        Node node2 = nodes.get(1);
        Node node3 = nodes.get(2);

        List<Connection> connections = List.of(dummyConnection(node1, node2),
                dummyConnection(node2, node3),
                dummyConnection(node3, node2));

        Set<String> orderedDegrees = metaDataCalculator.getOrderedDegrees(connections);

        assertThat(orderedDegrees).containsExactly(node1.getNodeCode(), node3.getNodeCode(), node2.getNodeCode());
    }

    @Test
    void testGetMaxNeighborsWithMoreData() {
        Node node1 = new Node();
        node1.setId("node1");

        Node node2 = new Node();
        node2.setId("node2");

        Connection connection1 = new Connection();
        connection1.setDepartureNode(node1);
        connection1.setArrivalNode(node2);

        Connection connection2 = new Connection();
        connection2.setDepartureNode(node2);
        connection2.setArrivalNode(node1);

        List<Node> nodes = Arrays.asList(node1, node2);
        List<Connection> connections = Arrays.asList(connection1, connection2);

        when(graphNodeCalculator.getNumberOfNeighbors(any(Node.class), anyList())).thenReturn(1L, 2L);

        Long maxNeighbors = metaDataCalculator.getMaxNeighbors(nodes, connections);

        assertThat(maxNeighbors).isEqualTo(2L);
        verify(graphNodeCalculator, times(2)).getNumberOfNeighbors(any(Node.class), eq(connections));
    }

    @Test
    void testGetMaxDegreeWithMoreData() {
        Node node1 = new Node();
        node1.setId("node1");

        Node node2 = new Node();
        node2.setId("node2");

        Connection connection1 = new Connection();
        connection1.setDepartureNode(node1);
        connection1.setArrivalNode(node2);

        Connection connection2 = new Connection();
        connection2.setDepartureNode(node2);
        connection2.setArrivalNode(node1);

        List<Node> nodes = Arrays.asList(node1, node2);
        List<Connection> connections = Arrays.asList(connection1, connection2);

        when(graphNodeCalculator.getDegree(any(Node.class), anyList())).thenReturn(1L, 2L);

        Long maxDegree = metaDataCalculator.getMaxDegree(nodes, connections);

        assertThat(maxDegree).isEqualTo(2L);
        verify(graphNodeCalculator, times(2)).getDegree(any(Node.class), eq(connections));
    }

    private List<Node> getNodes() {
        return List.of(dummyNode(), dummyNode(), dummyNode());
    }

}
