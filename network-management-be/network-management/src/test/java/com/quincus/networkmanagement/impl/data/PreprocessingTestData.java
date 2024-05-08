package com.quincus.networkmanagement.impl.data;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quincus.networkmanagement.api.constant.MmeGraphProperty;
import com.quincus.networkmanagement.api.constant.TrainingStatus;
import com.quincus.networkmanagement.api.constant.TrainingType;
import com.quincus.networkmanagement.api.domain.TrainingLog;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class PreprocessingTestData {
    static JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;

    public static ObjectNode dummyTrainingInput() {
        ObjectNode payload = new ObjectNode(jsonNodeFactory);
        ObjectNode graph = new ObjectNode(jsonNodeFactory);
        ObjectNode graphGraph = new ObjectNode(jsonNodeFactory);
        ArrayNode jsonNodes = jsonNodeFactory.arrayNode();
        jsonNodes.add(new ObjectNode(jsonNodeFactory));
        graphGraph.set(MmeGraphProperty.NODES, jsonNodes);
        graphGraph.set(MmeGraphProperty.EDGES, jsonNodeFactory.arrayNode());
        graph.set(MmeGraphProperty.META_DATA, jsonNodeFactory.objectNode());
        graph.set(MmeGraphProperty.GRAPH, graphGraph);
        payload.set(MmeGraphProperty.GRAPH, graph);
        payload.set(MmeGraphProperty.SETTINGS, jsonNodeFactory.objectNode());
        return payload;
    }

    public static ObjectNode dummyEmptyTrainingInput() {
        return new ObjectNode(jsonNodeFactory);
    }

    public static TrainingLog dummyTrainingLog() {
        TrainingLog trainingLog = new TrainingLog();
        trainingLog.setTrainingRequestId(UUID.randomUUID().toString());
        trainingLog.setUniqueId(UUID.randomUUID().toString());
        trainingLog.setStatus(TrainingStatus.INITIATED);
        trainingLog.setTrainingType(TrainingType.ON_NETWORK_CHANGE);
        return trainingLog;
    }
}
