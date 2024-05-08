package com.quincus.networkmanagement.impl.preprocessor.calculator.impl;

import com.quincus.networkmanagement.api.domain.Connection;
import com.quincus.networkmanagement.api.domain.Node;
import com.quincus.networkmanagement.impl.preprocessor.calculator.GraphNodeCalculator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GraphNodeCalculatorImpl implements GraphNodeCalculator {

    @Override
    public long getDegree(Node node, List<Connection> connectionList) {
        // return number of connections related to node
        return connectionList.stream().filter(c -> isNodeOfConnection(node, c)).count();
    }

    @Override
    public long getNumberOfNeighbors(Node node, List<Connection> connectionList) {
        // get all connections where departure node matches
        List<Connection> connectionsRelatedToNode = connectionList.stream().filter(c -> isDepartureNodeOfConnection(node, c)).toList();
        // return number of distinct arrival nodes
        return connectionsRelatedToNode.stream().map(c -> c.getArrivalNode().getId()).distinct().count();
    }

    /**
     * Return true if either departure or arrival node matches
     */
    private boolean isNodeOfConnection(Node node, Connection connection) {
        return isDepartureNodeOfConnection(node, connection) ||
                isArrivalNodeOfConnection(node, connection);
    }

    private boolean isDepartureNodeOfConnection(Node node, Connection connection) {
        return node.getId().equals(connection.getDepartureNode().getId());
    }

    private boolean isArrivalNodeOfConnection(Node node, Connection connection) {
        return node.getId().equals(connection.getArrivalNode().getId());
    }
}
