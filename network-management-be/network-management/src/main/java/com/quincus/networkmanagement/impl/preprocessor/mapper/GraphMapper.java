package com.quincus.networkmanagement.impl.preprocessor.mapper;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.quincus.networkmanagement.api.domain.Connection;
import com.quincus.networkmanagement.api.domain.Node;
import com.quincus.networkmanagement.impl.preprocessor.model.Edge;

import java.util.List;

public interface GraphMapper {
    ArrayNode toGraphNode(Node node, List<Connection> connectionList);

    ArrayNode toGraphEdge(Edge edge);
}
