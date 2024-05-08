package com.quincus.networkmanagement.impl.preprocessor.generator;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quincus.networkmanagement.api.domain.Connection;
import com.quincus.networkmanagement.api.domain.Node;

import java.util.List;

public interface GraphGenerator {

    ObjectNode toGraph(List<Node> nodes, List<Connection> connections, Long dateOfTraining);

}
