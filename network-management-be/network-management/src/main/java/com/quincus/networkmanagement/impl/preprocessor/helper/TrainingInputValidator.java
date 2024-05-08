package com.quincus.networkmanagement.impl.preprocessor.helper;

import com.fasterxml.jackson.databind.JsonNode;
import com.quincus.networkmanagement.api.constant.MmeGraphProperty;
import com.quincus.networkmanagement.api.exception.InvalidMmeGraphException;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TrainingInputValidator {

    private static final String ERR_NULL_GRAPH_PROPERTY = "Invalid graph, property '%s' is missing.";
    private static final String ERR_EMPTY_GRAPH_PROPERTY = "Invalid graph, property '%s' is empty or missing.";

    public static void validateTrainingInput(JsonNode payload) {
        validateGraph(payload);
        validateSettings(payload);
    }

    private void validateGraph(JsonNode payload) {
        JsonNode graph = payload.get(MmeGraphProperty.GRAPH);

        if (graph != null) {
            if (!graph.has(MmeGraphProperty.META_DATA)) {
                throw new InvalidMmeGraphException(String.format(ERR_NULL_GRAPH_PROPERTY, "graph.meta_data"));
            }
        } else {
            throw new InvalidMmeGraphException(String.format(ERR_NULL_GRAPH_PROPERTY, "graph"));
        }

        validateGraphGraph(graph);
    }

    private void validateGraphGraph(JsonNode graph) {
        JsonNode graphGraph = graph.get(MmeGraphProperty.GRAPH);

        if (graphGraph == null) {
            throw new InvalidMmeGraphException(String.format(ERR_NULL_GRAPH_PROPERTY, "graph.graph"));
        }

        validateGraphNodes(graphGraph);
        validateGraphEdges(graphGraph);
    }

    private void validateGraphNodes(JsonNode graphGraph) {
        JsonNode graphNodes = graphGraph.get(MmeGraphProperty.NODES);

        if (graphNodes == null || !graphNodes.isArray() || graphNodes.isEmpty()) {
            throw new InvalidMmeGraphException(String.format(ERR_EMPTY_GRAPH_PROPERTY, "graph.graph.nodes"));
        }
    }

    private void validateGraphEdges(JsonNode graphGraph) {
        JsonNode graphEdges = graphGraph.get(MmeGraphProperty.EDGES);
        if (graphEdges == null) {
            throw new InvalidMmeGraphException(String.format(ERR_NULL_GRAPH_PROPERTY, "graph.graph.edges"));
        }
    }

    private void validateSettings(JsonNode payload) {
        if (!payload.has(MmeGraphProperty.SETTINGS)) {
            throw new InvalidMmeGraphException(String.format(ERR_NULL_GRAPH_PROPERTY, "graph.settings"));
        }
    }
}
