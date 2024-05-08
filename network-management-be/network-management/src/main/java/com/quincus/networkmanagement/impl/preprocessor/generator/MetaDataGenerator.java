package com.quincus.networkmanagement.impl.preprocessor.generator;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quincus.networkmanagement.api.domain.Connection;
import com.quincus.networkmanagement.api.domain.Node;
import com.quincus.networkmanagement.impl.preprocessor.model.Edge;

import java.util.List;

public interface MetaDataGenerator {
    ObjectNode generateMetaData(List<Node> nodes, List<Connection> connections, List<Edge> edges);
}
