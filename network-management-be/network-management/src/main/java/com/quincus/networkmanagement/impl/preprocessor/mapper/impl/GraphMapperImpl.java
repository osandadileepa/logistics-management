package com.quincus.networkmanagement.impl.preprocessor.mapper.impl;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.quincus.networkmanagement.api.constant.DimensionUnit;
import com.quincus.networkmanagement.api.constant.VolumeUnit;
import com.quincus.networkmanagement.api.constant.WeightUnit;
import com.quincus.networkmanagement.api.domain.Connection;
import com.quincus.networkmanagement.api.domain.MeasurementUnits;
import com.quincus.networkmanagement.api.domain.Node;
import com.quincus.networkmanagement.api.domain.ShipmentProfileExtension;
import com.quincus.networkmanagement.impl.preprocessor.calculator.GraphNodeCalculator;
import com.quincus.networkmanagement.impl.preprocessor.mapper.GraphMapper;
import com.quincus.networkmanagement.impl.preprocessor.model.Edge;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.util.List;

import static com.quincus.networkmanagement.impl.preprocessor.helper.PreprocessingHelper.toUniformDimension;
import static com.quincus.networkmanagement.impl.preprocessor.helper.PreprocessingHelper.toUniformVolume;
import static com.quincus.networkmanagement.impl.preprocessor.helper.PreprocessingHelper.toUniformWeight;

@Component
@AllArgsConstructor
public class GraphMapperImpl implements GraphMapper {

    private static final JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;

    private final GraphNodeCalculator graphNodeCalculator;

    @Override
    public ArrayNode toGraphNode(Node node, List<Connection> connectionList) {

        ArrayNode result = jsonNodeFactory.arrayNode();
        ObjectNode properties = jsonNodeFactory.objectNode();

        properties.set("lat", DecimalNode.valueOf(node.getFacility().getLat()));
        properties.set("lon", DecimalNode.valueOf(node.getFacility().getLon()));
        properties.set("degree", LongNode.valueOf(graphNodeCalculator.getDegree(node, connectionList)));
        properties.set("number_of_neighbors", LongNode.valueOf(graphNodeCalculator.getNumberOfNeighbors(node, connectionList)));

        result.insert(0, node.getNodeCode());
        result.insert(1, properties);

        return result;
    }

    @Override
    public ArrayNode toGraphEdge(Edge edge) {
        ArrayNode result = jsonNodeFactory.arrayNode();
        ObjectNode properties = jsonNodeFactory.objectNode();

        properties.set("flight_number", TextNode.valueOf(edge.getFlightNumber()));
        properties.set("departure_hub", TextNode.valueOf(edge.getDepartureHub()));
        properties.set("departure_lat", DoubleNode.valueOf(edge.getDepartureLat()));
        properties.set("departure_lon", DoubleNode.valueOf(edge.getDepartureLon()));
        properties.set("departure_time", LongNode.valueOf(edge.getDepartureTime()));
        properties.set("arrival_hub", TextNode.valueOf(edge.getArrivalHub()));
        properties.set("arrival_lat", DoubleNode.valueOf(edge.getArrivalLat()));
        properties.set("arrival_lon", DoubleNode.valueOf(edge.getArrivalLon()));
        properties.set("arrival_time", LongNode.valueOf(edge.getArrivalTime()));
        properties.set("vehicle_model", TextNode.valueOf(""));
        properties.set("vehicle_type", TextNode.valueOf(edge.getVehicleType()));
        properties.set("distance", DecimalNode.valueOf(edge.getDistance().setScale(4, RoundingMode.DOWN)));
        properties.set("duration", IntNode.valueOf(edge.getDuration()));
        properties.set("cost", DecimalNode.valueOf(edge.getCost()));
        properties.set("capacity", IntNode.valueOf(edge.getCapacity()));
        properties.set("networkx_id", LongNode.valueOf(0));
        properties.set("shipment_profiles", toGraphShipmentProfile(edge.getShipmentProfiles(), edge.getMeasurementUnits()));
        properties.set("co2_emissions", DecimalNode.valueOf(edge.getCo2Emissions()));

        result.insert(0, edge.getDepartureHub());
        result.insert(1, edge.getArrivalHub());
        result.insert(2, properties);

        return result;
    }

    private ObjectNode toGraphShipmentProfile(ShipmentProfileExtension shipmentProfile, MeasurementUnits measurementUnits) {
        ObjectNode result = new ObjectNode(jsonNodeFactory);

        DimensionUnit dimensionUnit = measurementUnits.getDimensionUnit();
        WeightUnit weightUnit = measurementUnits.getWeightUnit();
        VolumeUnit volumeUnit = measurementUnits.getVolumeUnit();

        result.set("max_length", DecimalNode.valueOf(toUniformDimension(shipmentProfile.getMaxLength(), dimensionUnit)));
        result.set("min_length", DecimalNode.valueOf(toUniformDimension(shipmentProfile.getMinLength(), dimensionUnit)));
        result.set("max_width", DecimalNode.valueOf(toUniformDimension(shipmentProfile.getMaxWidth(), dimensionUnit)));
        result.set("min_width", DecimalNode.valueOf(toUniformDimension(shipmentProfile.getMinWidth(), dimensionUnit)));
        result.set("max_height", DecimalNode.valueOf(toUniformDimension(shipmentProfile.getMaxHeight(), dimensionUnit)));
        result.set("min_height", DecimalNode.valueOf(toUniformDimension(shipmentProfile.getMinHeight(), dimensionUnit)));
        result.set("max_weight", DecimalNode.valueOf(toUniformWeight(shipmentProfile.getMaxWeight(), weightUnit)));
        result.set("min_weight", DecimalNode.valueOf(toUniformWeight(shipmentProfile.getMinWeight(), weightUnit)));
        result.set("max_single_side", DecimalNode.valueOf(toUniformDimension(shipmentProfile.getMaxSingleSide(), dimensionUnit)));
        result.set("min_single_side", DecimalNode.valueOf(toUniformDimension(shipmentProfile.getMinSingleSide(), dimensionUnit)));
        result.set("max_linear_dim", DecimalNode.valueOf(toUniformDimension(shipmentProfile.getMaxLinearDim(), dimensionUnit)));
        result.set("min_linear_dim", DecimalNode.valueOf(toUniformDimension(shipmentProfile.getMinLinearDim(), dimensionUnit)));
        result.set("max_volume", DecimalNode.valueOf(toUniformVolume(shipmentProfile.getMaxVolume(), volumeUnit)));
        result.set("min_volume", DecimalNode.valueOf(toUniformVolume(shipmentProfile.getMinVolume(), volumeUnit)));

        return result;
    }

}
