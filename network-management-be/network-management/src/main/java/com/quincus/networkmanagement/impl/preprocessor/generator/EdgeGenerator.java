package com.quincus.networkmanagement.impl.preprocessor.generator;

import com.quincus.networkmanagement.api.domain.Connection;
import com.quincus.networkmanagement.impl.preprocessor.model.Edge;

import java.util.List;

public interface EdgeGenerator {
    List<Edge> generateEdges(List<Connection> connections, Long dateOfTraining);
}
