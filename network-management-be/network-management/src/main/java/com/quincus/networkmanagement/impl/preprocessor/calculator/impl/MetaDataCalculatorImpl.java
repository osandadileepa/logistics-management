package com.quincus.networkmanagement.impl.preprocessor.calculator.impl;

import com.quincus.networkmanagement.api.constant.DistanceUnit;
import com.quincus.networkmanagement.api.domain.Connection;
import com.quincus.networkmanagement.api.domain.Facility;
import com.quincus.networkmanagement.api.domain.FacilityPair;
import com.quincus.networkmanagement.api.domain.Node;
import com.quincus.networkmanagement.impl.preprocessor.calculator.GraphNodeCalculator;
import com.quincus.networkmanagement.impl.preprocessor.calculator.MetaDataCalculator;
import com.quincus.networkmanagement.impl.service.calculator.DistanceCalculator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.quincus.networkmanagement.impl.preprocessor.helper.PreprocessingHelper.toUniformCost;

@Component
@AllArgsConstructor
public class MetaDataCalculatorImpl implements MetaDataCalculator {
    private final DistanceCalculator distanceCalculator;
    private final GraphNodeCalculator graphNodeCalculator;

    @Override
    public List<Double> getSortedCost(List<Connection> connections) {
        return connections.stream()
                .map(c -> toUniformCost(c.getCost(), c.getCurrency()).doubleValue())
                .sorted(Comparator.reverseOrder()).toList();
    }

    @Override
    public Set<Double> getSortedDistance(List<Node> nodes) {
        List<FacilityPair<Facility, Facility>> uniquePairs = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                if (!nodes.get(i).getId().equals(nodes.get(j).getId())) {
                    uniquePairs.add(new FacilityPair<>(nodes.get(i).getFacility(), nodes.get(j).getFacility()));
                }
            }
        }

        return uniquePairs.parallelStream()
                .map(facilityPair -> distanceCalculator.calculateDistance(new Node(facilityPair.arrival()),
                        new Node(facilityPair.departure()), DistanceUnit.KILOMETERS))
                .map(BigDecimal::doubleValue)
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Integer getNumberOfNodes(List<Node> nodes) {
        return nodes.size();
    }

    @Override
    public List<String> getBannedOrigins(List<Node> nodes, List<Connection> connections) {
        List<String> arrivalNodeCodeList = getArrivalNodeCodeList(connections);
        List<String> departureNodeCodeList = getDepartureNodeCodeList(connections);

        return getNodeCodeList(nodes).parallelStream()
                .filter(id -> arrivalNodeCodeList.contains(id) && !departureNodeCodeList.contains(id)).toList();
    }

    @Override
    public List<String> getBannedDestinations(List<Node> nodes, List<Connection> connections) {
        List<String> arrivalNodeCodeList = getArrivalNodeCodeList(connections);
        List<String> departureNodeCodeList = getDepartureNodeCodeList(connections);

        return getNodeCodeList(nodes).parallelStream()
                .filter(id -> departureNodeCodeList.contains(id) && !arrivalNodeCodeList.contains(id)).toList();
    }

    @Override
    public Long getMaxNeighbors(List<Node> nodes, List<Connection> connections) {
        return nodes.stream()
                .map(node -> graphNodeCalculator.getNumberOfNeighbors(node, connections))
                .parallel()
                .max(Long::compareTo).orElse(0L);
    }

    @Override
    public Long getMaxDegree(List<Node> nodes, List<Connection> connections) {
        return nodes.stream()
                .map(node -> graphNodeCalculator.getDegree(node, connections))
                .parallel()
                .max(Long::compareTo).orElse(0L);
    }

    @Override
    public Set<String> getOrderedInDegrees(List<Connection> connections) {
        return connections.parallelStream()
                .map(Connection::getArrivalNode)
                .collect(Collectors.groupingBy(Node::getNodeCode, Collectors.counting()))
                .entrySet()
                .parallelStream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new)).keySet();
    }

    @Override
    public Set<String> getOrderedOutDegrees(List<Connection> connections) {
        return connections.parallelStream()
                .map(Connection::getDepartureNode)
                .collect(Collectors.groupingBy(Node::getNodeCode, Collectors.counting()))
                .entrySet()
                .parallelStream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new)).keySet();
    }

    @Override
    public Set<String> getOrderedDegrees(List<Connection> connections) {
        return connections.parallelStream()
                .flatMap(conn -> Stream.of(conn.getArrivalNode(), conn.getDepartureNode()))
                .collect(Collectors.groupingBy(Node::getNodeCode, Collectors.counting()))
                .entrySet()
                .parallelStream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new)).keySet();
    }

    private List<String> getNodeCodeList(List<Node> nodes) {
        return nodes.stream().map(Node::getNodeCode).toList();
    }

    private List<String> getArrivalNodeCodeList(List<Connection> connections) {
        return connections.stream().map(c -> c.getArrivalNode().getNodeCode()).distinct().toList();
    }

    private List<String> getDepartureNodeCodeList(List<Connection> connections) {
        return connections.stream().map(c -> c.getDepartureNode().getNodeCode()).distinct().toList();
    }
}

