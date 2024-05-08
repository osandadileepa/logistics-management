package com.quincus.networkmanagement.impl.preprocessor.generator;

import com.quincus.networkmanagement.api.domain.Connection;
import com.quincus.networkmanagement.impl.parser.RRuleParser;
import com.quincus.networkmanagement.impl.preprocessor.generator.impl.EdgeGeneratorImpl;
import com.quincus.networkmanagement.impl.preprocessor.mapper.EdgeMapper;
import com.quincus.networkmanagement.impl.preprocessor.model.Edge;
import org.dmfs.rfc5545.DateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import static com.quincus.networkmanagement.impl.data.NetworkManagementTestData.dummyAirConnection;
import static com.quincus.networkmanagement.impl.data.NetworkManagementTestData.dummyGroundConnection;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {EdgeGeneratorImpl.class})
class EdgeGeneratorTest {
    @Mock
    private RRuleParser rRuleParser;
    @Mock
    private EdgeMapper edgeMapper;
    @InjectMocks
    private EdgeGeneratorImpl edgeGenerator;

    @Test
    void testGenerateEdges() {
        Connection connection1 = dummyAirConnection();
        Connection connection2 = dummyGroundConnection();

        connection2.getDepartureNode().setTimezone("Asia/Jakarta UTC+07:00");

        List<Connection> connections = List.of(connection1, connection2);

        List<DateTime> dummyTimings = List.of(new DateTime(1593000000), new DateTime(1938600000));

        Edge mockEdge1 = new Edge();
        Edge mockEdge2 = new Edge();

        when(rRuleParser.generateTimingsFromSchedules(connection1.getSchedules(), TimeZone.getTimeZone("Asia/Manila"), null)).thenReturn(dummyTimings);
        when(rRuleParser.generateTimingsFromSchedules(connection2.getSchedules(), TimeZone.getTimeZone("Asia/Jakarta"), null)).thenReturn(dummyTimings);
        when(edgeMapper.toEdge(connection1)).thenReturn(mockEdge1);
        when(edgeMapper.toEdge(connection2)).thenReturn(mockEdge2);

        List<Edge> edges = edgeGenerator.generateEdges(connections, null);

        assertThat(edges).isNotNull().isNotEmpty().hasSize(4);
        assertThat(edges.get(0)).isSameAs(mockEdge1);
        assertThat(edges.get(1)).isSameAs(mockEdge1);
        assertThat(edges.get(2)).isSameAs(mockEdge2);
        assertThat(edges.get(3)).isSameAs(mockEdge2);

        verify(edgeMapper, times(4)).toEdge(any());
    }

    @Test
    void testGenerateEdgesWithNoConnections() {
        List<Connection> connections = new ArrayList<>();

        List<Edge> edges = edgeGenerator.generateEdges(connections, null);

        assertThat(edges).isEmpty();
    }
}

