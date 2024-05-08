package com.quincus.networkmanagement.impl.preprocessor.calculator;

import com.quincus.networkmanagement.api.domain.Connection;
import com.quincus.networkmanagement.api.domain.Node;

import java.util.List;

/**
 * Required pre-training business logic under graph.nodes
 */
public interface GraphNodeCalculator {
    long getDegree(Node node, List<Connection> connectionList);

    long getNumberOfNeighbors(Node node, List<Connection> connectionList);
}
