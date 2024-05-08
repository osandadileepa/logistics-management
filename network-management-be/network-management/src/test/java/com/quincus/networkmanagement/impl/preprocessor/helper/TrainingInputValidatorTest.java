package com.quincus.networkmanagement.impl.preprocessor.helper;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quincus.networkmanagement.api.constant.MmeGraphProperty;
import com.quincus.networkmanagement.api.exception.InvalidMmeGraphException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ContextConfiguration;

import static com.quincus.networkmanagement.impl.data.PreprocessingTestData.dummyEmptyTrainingInput;
import static com.quincus.networkmanagement.impl.data.PreprocessingTestData.dummyTrainingInput;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {TrainingInputValidator.class})
class TrainingInputValidatorTest {

    @Test
    void testValidateTrainingInputWithValidPayload() {
        ObjectNode payload = dummyTrainingInput();
        TrainingInputValidator.validateTrainingInput(payload);
    }

    @Test
    void testValidateTrainingInputWithMissingGraph() {
        ObjectNode payload = dummyEmptyTrainingInput();

        assertThatThrownBy(() -> TrainingInputValidator.validateTrainingInput(payload))
                .isInstanceOf(InvalidMmeGraphException.class)
                .hasMessageContaining("Invalid graph, property 'graph' is missing.");
    }

    @Test
    void testValidateTrainingInputWithMissingGraphMetaData() {
        ObjectNode payload = dummyTrainingInput();
        ObjectNode graph = (ObjectNode) payload.get(MmeGraphProperty.GRAPH);
        graph.remove(MmeGraphProperty.META_DATA);

        assertThatThrownBy(() -> TrainingInputValidator.validateTrainingInput(payload))
                .isInstanceOf(InvalidMmeGraphException.class)
                .hasMessageContaining("Invalid graph, property 'graph.meta_data' is missing.");
    }

    @Test
    void testValidateTrainingInputWithMissingGraphGraph() {
        ObjectNode payload = dummyTrainingInput();
        ObjectNode graph = (ObjectNode) payload.get(MmeGraphProperty.GRAPH);
        graph.remove(MmeGraphProperty.GRAPH);

        assertThatThrownBy(() -> TrainingInputValidator.validateTrainingInput(payload))
                .isInstanceOf(InvalidMmeGraphException.class)
                .hasMessageContaining("Invalid graph, property 'graph.graph' is missing.");
    }

    @Test
    void testValidateTrainingInputWithMissingGraphNodes() {
        ObjectNode payload = dummyTrainingInput();
        ObjectNode graphGraph = (ObjectNode) payload.get(MmeGraphProperty.GRAPH).get(MmeGraphProperty.GRAPH);
        graphGraph.remove(MmeGraphProperty.NODES);

        assertThatThrownBy(() -> TrainingInputValidator.validateTrainingInput(payload))
                .isInstanceOf(InvalidMmeGraphException.class)
                .hasMessageContaining("Invalid graph, property 'graph.graph.nodes' is empty or missing.");
    }

    @Test
    void testValidateTrainingInputWithEmptyGraphNodes() {
        ObjectNode payload = dummyTrainingInput();
        ObjectNode graphGraph = (ObjectNode) payload.get(MmeGraphProperty.GRAPH).get(MmeGraphProperty.GRAPH);
        graphGraph.remove(MmeGraphProperty.NODES);

        assertThatThrownBy(() -> TrainingInputValidator.validateTrainingInput(payload))
                .isInstanceOf(InvalidMmeGraphException.class)
                .hasMessageContaining("Invalid graph, property 'graph.graph.nodes' is empty or missing.");
    }

    @Test
    void testValidateTrainingInputWithMissingGraphEdges() {
        ObjectNode payload = dummyTrainingInput();
        ObjectNode graphGraph = (ObjectNode) payload.get(MmeGraphProperty.GRAPH).get(MmeGraphProperty.GRAPH);
        graphGraph.remove(MmeGraphProperty.EDGES);

        assertThatThrownBy(() -> TrainingInputValidator.validateTrainingInput(payload))
                .isInstanceOf(InvalidMmeGraphException.class)
                .hasMessageContaining("Invalid graph, property 'graph.graph.edges' is missing.");
    }

    @Test
    void testValidateTrainingInputWithInvalidSettings() {
        ObjectNode payload = dummyTrainingInput();
        payload.remove(MmeGraphProperty.SETTINGS);

        assertThatThrownBy(() -> TrainingInputValidator.validateTrainingInput(payload))
                .isInstanceOf(InvalidMmeGraphException.class)
                .hasMessageContaining("Invalid graph, property 'graph.settings' is missing.");
    }


}


