package com.quincus.networkmanagement.impl.preprocessor.generator.impl;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quincus.networkmanagement.api.domain.Connection;
import com.quincus.networkmanagement.api.domain.Node;
import com.quincus.networkmanagement.impl.preprocessor.calculator.MetaDataCalculator;
import com.quincus.networkmanagement.impl.preprocessor.generator.MetaDataGenerator;
import com.quincus.networkmanagement.impl.preprocessor.model.Edge;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Component
@AllArgsConstructor
public class MetaDataGeneratorImpl implements MetaDataGenerator {
    private static final JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;
    private final MetaDataCalculator metaDataCalculator;

    @Override
    public ObjectNode generateMetaData(List<Node> nodes, List<Connection> connections, List<Edge> edges) {
        ObjectNode metaData = jsonNodeFactory.objectNode();
        ArrayNode topCost = jsonNodeFactory.arrayNode();
        ArrayNode topDistance = jsonNodeFactory.arrayNode();
        ArrayNode topTime = jsonNodeFactory.arrayNode();
        ArrayNode bottomTime = jsonNodeFactory.arrayNode();
        ArrayNode bannedOrigins = jsonNodeFactory.arrayNode();
        ArrayNode bannedDestinations = jsonNodeFactory.arrayNode();
        ArrayNode orderedInDegrees = jsonNodeFactory.arrayNode();
        ArrayNode orderedOutDegrees = jsonNodeFactory.arrayNode();
        ArrayNode orderedDegrees = jsonNodeFactory.arrayNode();

        List<Double> sortedCost = metaDataCalculator.getSortedCost(connections);
        sortedCost.stream().limit(6).forEach(topCost::add);

        metaData.put("max_cost", sortedCost.get(0));
        metaData.set("top_cost", topCost);

        Set<Double> sortedDistance = metaDataCalculator.getSortedDistance(nodes);
        sortedDistance.stream().limit(6).forEach(topDistance::add);

        metaData.put("max_distance", sortedDistance.stream().toList().get(0));
        metaData.set("top_distance", topDistance);

        List<Long> sortedArrivalTime = getSortedArrivalTime(edges);
        List<Long> sortedDepartureTime = getSortedDepartureTime(edges);
        sortedArrivalTime.stream().limit(6).forEach(topTime::add);
        sortedDepartureTime.stream().limit(6).forEach(bottomTime::add);

        metaData.put("max_time", sortedArrivalTime.get(0));
        metaData.set("top_time", topTime);
        metaData.put("min_time", sortedDepartureTime.get(0));
        metaData.set("bottom_time", bottomTime);

        metaData.put("n_nodes", metaDataCalculator.getNumberOfNodes(nodes));
        metaData.put("n_edges", edges.size());

        metaDataCalculator.getBannedOrigins(nodes, connections).forEach(bannedOrigins::add);
        metaDataCalculator.getBannedDestinations(nodes, connections).forEach(bannedDestinations::add);

        metaData.set("banned_origins", bannedOrigins);
        metaData.set("banned_destinations", bannedDestinations);

        metaData.put("max_neighbors", metaDataCalculator.getMaxNeighbors(nodes, connections));
        metaData.put("max_degree", metaDataCalculator.getMaxDegree(nodes, connections));

        metaDataCalculator.getOrderedInDegrees(connections).forEach(orderedInDegrees::add);
        metaDataCalculator.getOrderedOutDegrees(connections).forEach(orderedOutDegrees::add);
        metaDataCalculator.getOrderedDegrees(connections).forEach(orderedDegrees::add);

        metaData.set("ordered_in_degrees", orderedInDegrees);
        metaData.set("ordered_out_degrees", orderedOutDegrees);
        metaData.set("ordered_degrees", orderedDegrees);

        metaData.put("cost_type", "nw");

        return metaData;
    }

    private List<Long> getSortedArrivalTime(List<Edge> edges) {
        return edges.parallelStream().map(Edge::getArrivalTime).sorted(Comparator.reverseOrder()).toList();
    }

    private List<Long> getSortedDepartureTime(List<Edge> edges) {
        return edges.parallelStream().map(Edge::getDepartureTime).sorted().toList();
    }
}
