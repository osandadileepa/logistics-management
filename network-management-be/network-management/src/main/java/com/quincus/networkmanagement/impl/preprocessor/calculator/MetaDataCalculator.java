package com.quincus.networkmanagement.impl.preprocessor.calculator;

import com.quincus.networkmanagement.api.domain.Connection;
import com.quincus.networkmanagement.api.domain.Node;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

/**
 * Training meta_data for a graph
 */
public interface MetaDataCalculator {
    List<Double> getSortedCost(@NotNull List<Connection> connections);

    Set<Double> getSortedDistance(@NotNull List<Node> nodes);

    Integer getNumberOfNodes(@NotNull List<Node> nodes);

    List<String> getBannedOrigins(@NotNull List<Node> nodes, @NotNull List<Connection> connections);

    List<String> getBannedDestinations(@NotNull List<Node> nodes, @NotNull List<Connection> connections);

    Long getMaxNeighbors(@NotNull List<Node> nodes, @NotNull List<Connection> connections);

    Long getMaxDegree(@NotNull List<Node> nodes, @NotNull List<Connection> connections);

    Set<String> getOrderedInDegrees(@NotNull List<Connection> connections);

    Set<String> getOrderedOutDegrees(@NotNull List<Connection> connections);

    Set<String> getOrderedDegrees(@NotNull List<Connection> connections);
}
