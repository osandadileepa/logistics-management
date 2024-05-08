package com.quincus.networkmanagement.impl.preprocessor.generator;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quincus.networkmanagement.api.domain.Connection;
import com.quincus.networkmanagement.api.domain.Node;
import com.quincus.networkmanagement.impl.preprocessor.generator.impl.GraphGeneratorImpl;
import com.quincus.networkmanagement.impl.preprocessor.mapper.GraphMapper;
import com.quincus.networkmanagement.impl.preprocessor.model.Edge;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static com.quincus.networkmanagement.impl.data.NetworkManagementTestData.dummyAirConnection;
import static com.quincus.networkmanagement.impl.data.NetworkManagementTestData.dummyEdge;
import static com.quincus.networkmanagement.impl.data.NetworkManagementTestData.dummyGroundConnection;
import static com.quincus.networkmanagement.impl.data.NetworkManagementTestData.dummyNode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {GraphGenerator.class})
class GraphGeneratorTest {

    @Mock
    private GraphMapper graphMapper;
    @Mock
    private EdgeGenerator edgeGenerator;
    @Mock
    private MetaDataGenerator metaDataGenerator;

    private GraphGenerator graphGenerator;

    @BeforeEach
    public void setup() {
        graphGenerator = new GraphGeneratorImpl(graphMapper, edgeGenerator, metaDataGenerator);
    }

    @Test
    void testToGraph() {

        Node node1 = dummyNode();
        Node node2 = dummyNode();
        Connection connection1 = dummyAirConnection();
        Connection connection2 = dummyGroundConnection();
        Edge edge1 = dummyEdge();
        Edge edge2 = dummyEdge();

        List<Node> nodes = List.of(node1, node2);
        List<Connection> connections = List.of(connection1, connection2);
        List<Edge> edges = List.of(edge1, edge2);

        ArrayNode graphNode = new ArrayNode(JsonNodeFactory.instance);
        ObjectNode metaData = new ObjectNode(JsonNodeFactory.instance);

        when(graphMapper.toGraphNode(any(Node.class), anyList())).thenReturn(graphNode);
        when(metaDataGenerator.generateMetaData(anyList(), anyList(), anyList())).thenReturn(metaData);
        when(edgeGenerator.generateEdges(connections, null)).thenReturn(edges);

        ObjectNode result = graphGenerator.toGraph(nodes, connections, null);

        assertThat(result).isNotNull().isExactlyInstanceOf(ObjectNode.class);
        assertThat(result.get("graph")).isNotNull();
        assertThat(result.get("meta_data")).isNotNull();
        assertThat(result.get("graph").get("edges")).isNotNull();
        assertThat(result.get("graph").get("nodes")).isNotNull();
        assertThat(result.get("graph").get("edges").get(edge1.getDepartureHub())).isNotNull();
        assertThat(result.get("graph").get("edges").get(edge2.getDepartureHub())).isNotNull();

        verify(graphMapper, times(nodes.size())).toGraphNode(any(Node.class), anyList());
        verify(metaDataGenerator).generateMetaData(anyList(), anyList(), anyList());
    }
}

