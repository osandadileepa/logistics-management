package com.quincus.networkmanagement.impl.preprocessor.generator.impl;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quincus.networkmanagement.api.constant.MmeGraphProperty;
import com.quincus.networkmanagement.api.domain.Connection;
import com.quincus.networkmanagement.api.domain.Node;
import com.quincus.networkmanagement.impl.preprocessor.generator.EdgeGenerator;
import com.quincus.networkmanagement.impl.preprocessor.generator.GraphGenerator;
import com.quincus.networkmanagement.impl.preprocessor.generator.MetaDataGenerator;
import com.quincus.networkmanagement.impl.preprocessor.mapper.GraphMapper;
import com.quincus.networkmanagement.impl.preprocessor.model.Edge;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class GraphGeneratorImpl implements GraphGenerator {

    private static final JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;
    private final GraphMapper graphMapper;
    private final EdgeGenerator edgeGenerator;
    private final MetaDataGenerator metaDataGenerator;

    @Override
    public ObjectNode toGraph(List<Node> nodes, List<Connection> connections, Long dateOfTraining) {

        List<Edge> edges = edgeGenerator.generateEdges(connections, dateOfTraining);

        ObjectNode result = jsonNodeFactory.objectNode();
        ObjectNode graph = jsonNodeFactory.objectNode();

        graph.set(MmeGraphProperty.NODES, getNodes(nodes, connections));
        graph.set(MmeGraphProperty.EDGES, getEdges(edges));

        result.set(MmeGraphProperty.GRAPH, graph);
        result.set(MmeGraphProperty.META_DATA, metaDataGenerator.generateMetaData(nodes, connections, edges));

        return result;
    }

    private ArrayNode getNodes(List<Node> nodes, List<Connection> connections) {
        ArrayNode graphNodes = jsonNodeFactory.arrayNode();
        graphNodes.addAll(
                nodes.stream().map(n -> graphMapper.toGraphNode(n, connections)).toList()
        );
        return graphNodes;
    }

    private ObjectNode getEdges(List<Edge> edges) {

        Map<String, List<Edge>> edgeMap = edges.stream().collect(Collectors.groupingBy(Edge::getDepartureHub));

        ObjectNode graphEdges = jsonNodeFactory.objectNode();

        for (Map.Entry<String, List<Edge>> entry : edgeMap.entrySet()) {
            ArrayNode edgesByDepartureHub = jsonNodeFactory.arrayNode();
            edgesByDepartureHub.addAll(
                    entry.getValue().stream().map(graphMapper::toGraphEdge).toList()
            );
            graphEdges.set(entry.getKey(), edgesByDepartureHub);
        }

        return graphEdges;
    }

}
