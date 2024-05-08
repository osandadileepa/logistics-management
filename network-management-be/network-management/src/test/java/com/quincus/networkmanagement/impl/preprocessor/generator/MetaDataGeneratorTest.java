package com.quincus.networkmanagement.impl.preprocessor.generator;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quincus.networkmanagement.api.domain.Connection;
import com.quincus.networkmanagement.api.domain.Node;
import com.quincus.networkmanagement.impl.preprocessor.calculator.MetaDataCalculator;
import com.quincus.networkmanagement.impl.preprocessor.generator.impl.MetaDataGeneratorImpl;
import com.quincus.networkmanagement.impl.preprocessor.model.Edge;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {MetaDataGeneratorImpl.class})
class MetaDataGeneratorTest {
    @Mock
    private MetaDataCalculator metaDataCalculator;

    @Autowired
    private MetaDataGenerator metaDataGenerator;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        metaDataGenerator = new MetaDataGeneratorImpl(metaDataCalculator);
    }

    @Test
    void testToMetaData() {
        List<Node> nodes = new ArrayList<>();
        List<Connection> connections = new ArrayList<>();
        List<Edge> edges = new ArrayList<>(getEdges());
        double[] costs = new double[]{10.0, 8.0, 5.0};
        double[] distances = new double[]{50.0, 30.0, 10.0};
        Set<String> orderedIn = new LinkedHashSet<>();
        orderedIn.add("in1");
        orderedIn.add("in2");
        Set<String> orderedOut = new LinkedHashSet<>();
        orderedOut.add("out1");
        orderedOut.add("out2");
        Set<String> ordered = new LinkedHashSet<>();
        ordered.add("deg1");
        ordered.add("deg2");

        when(metaDataCalculator.getSortedCost(connections)).thenReturn(Arrays.stream(costs).boxed().toList());
        when(metaDataCalculator.getSortedDistance(nodes)).thenReturn(Arrays.stream(distances).boxed().collect(Collectors.toCollection(LinkedHashSet::new)));
        when(metaDataCalculator.getNumberOfNodes(nodes)).thenReturn(10);
        when(metaDataCalculator.getBannedOrigins(nodes, connections)).thenReturn(List.of("origin1", "origin2"));
        when(metaDataCalculator.getBannedDestinations(nodes, connections)).thenReturn(List.of("dest1", "dest2"));
        when(metaDataCalculator.getMaxNeighbors(nodes, connections)).thenReturn(3L);
        when(metaDataCalculator.getMaxDegree(nodes, connections)).thenReturn(2L);
        when(metaDataCalculator.getOrderedInDegrees(connections)).thenReturn(orderedIn);
        when(metaDataCalculator.getOrderedOutDegrees(connections)).thenReturn(orderedOut);
        when(metaDataCalculator.getOrderedDegrees(connections)).thenReturn(ordered);

        ObjectNode actualMetaData = metaDataGenerator.generateMetaData(nodes, connections, edges);

        JsonNodeFactory factory = JsonNodeFactory.instance;
        ObjectNode expectedMetaData = factory.objectNode();
        ArrayNode topCost = factory.arrayNode();
        ArrayNode topDistance = factory.arrayNode();
        ArrayNode bannedOrigins = factory.arrayNode();
        ArrayNode bannedDestinations = factory.arrayNode();
        ArrayNode orderedInDegrees = factory.arrayNode();
        ArrayNode orderedOutDegrees = factory.arrayNode();
        ArrayNode orderedDegrees = factory.arrayNode();
        ArrayNode bottomTime = factory.arrayNode();
        ArrayNode topTime = factory.arrayNode();

        Arrays.stream(costs).forEach(topCost::add);
        Arrays.stream(distances).forEach(topDistance::add);
        orderedIn.forEach(orderedInDegrees::add);
        orderedOut.forEach(orderedOutDegrees::add);
        ordered.forEach(orderedDegrees::add);
        edges.stream().map(Edge::getDepartureTime).forEach(bottomTime::add);
        Collections.reverse(edges);
        edges.stream().map(Edge::getArrivalTime).toList().forEach(topTime::add);

        expectedMetaData.put("max_cost", 10.0);
        expectedMetaData.set("top_cost", topCost);
        expectedMetaData.put("max_distance", 50.0);
        expectedMetaData.set("top_distance", topDistance);
        expectedMetaData.put("max_time", 203);
        expectedMetaData.set("top_time", topTime);
        expectedMetaData.put("min_time", 101);
        expectedMetaData.set("bottom_time", bottomTime);
        expectedMetaData.put("n_nodes", 10);
        expectedMetaData.put("n_edges", 3);
        bannedOrigins.add("origin1");
        bannedOrigins.add("origin2");
        bannedDestinations.add("dest1");
        bannedDestinations.add("dest2");
        expectedMetaData.set("banned_origins", bannedOrigins);
        expectedMetaData.set("banned_destinations", bannedDestinations);
        expectedMetaData.put("max_neighbors", 3);
        expectedMetaData.put("max_degree", 2);
        expectedMetaData.set("ordered_in_degrees", orderedInDegrees);
        expectedMetaData.set("ordered_out_degrees", orderedOutDegrees);
        expectedMetaData.set("ordered_degrees", orderedDegrees);
        expectedMetaData.put("cost_type", "nw");

        assertThat(actualMetaData).isNotEmpty().isNotNull().isExactlyInstanceOf(ObjectNode.class);

        assertThat(actualMetaData.toString().replaceAll("\\s", ""))
                .isEqualTo(expectedMetaData.toString().replaceAll("\\s", ""));
    }

    private List<Edge> getEdges() {
        Edge edge1 = new Edge();
        edge1.setDepartureTime(101);
        edge1.setArrivalTime(201);
        Edge edge2 = new Edge();
        edge2.setDepartureTime(102);
        edge2.setArrivalTime(202);
        Edge edge3 = new Edge();
        edge3.setDepartureTime(103);
        edge3.setArrivalTime(203);

        return List.of(edge1, edge2, edge3);
    }
}

