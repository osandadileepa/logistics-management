package com.quincus.networkmanagement.impl.preprocessor.generator.impl;

import com.quincus.networkmanagement.api.domain.Connection;
import com.quincus.networkmanagement.impl.parser.RRuleParser;
import com.quincus.networkmanagement.impl.preprocessor.generator.EdgeGenerator;
import com.quincus.networkmanagement.impl.preprocessor.mapper.EdgeMapper;
import com.quincus.networkmanagement.impl.preprocessor.model.Edge;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.dmfs.rfc5545.DateTime;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import static com.quincus.networkmanagement.impl.preprocessor.helper.PreprocessingHelper.toArrivalTime;
import static com.quincus.networkmanagement.impl.preprocessor.helper.PreprocessingHelper.toDepartureTime;
import static com.quincus.networkmanagement.impl.preprocessor.helper.PreprocessingHelper.toUniformCost;

@Component
@AllArgsConstructor
public class EdgeGeneratorImpl implements EdgeGenerator {
    private final RRuleParser rRuleParser;
    private final EdgeMapper edgeMapper;

    @Override
    public List<Edge> generateEdges(List<Connection> connections, Long dateOfTraining) {
        List<Edge> edges = new ArrayList<>();
        connections.forEach(
                c -> edges.addAll(toEdges(c, dateOfTraining))
        );
        return edges;
    }

    private List<Edge> toEdges(Connection connection, Long dateOfTraining) {
        List<Edge> edges = new ArrayList<>();

        TimeZone timeZone = StringUtils.isNotBlank(connection.getDepartureNode().getTimezone()) ?
                TimeZone.getTimeZone(connection.getDepartureNode().getTimezone().split(" ", 2)[0]) : TimeZone.getDefault();

        List<DateTime> timings = rRuleParser.generateTimingsFromSchedules(connection.getSchedules(), timeZone, dateOfTraining);
        BigDecimal uniformCost = toUniformCost(connection.getCost(), connection.getCurrency());

        timings.forEach(t -> {
            Edge edge = edgeMapper.toEdge(connection);
            edge.setDepartureTime(toDepartureTime(t));
            edge.setArrivalTime(toArrivalTime(t, connection.getDuration()));
            edge.setCost(uniformCost);

            edges.add(edge);
        });

        return edges;
    }
}
