package com.quincus.mme.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quincus.mme.model.MmeGetTrainedModelRequest;
import com.quincus.mme.model.MmeTrainModelResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MmeApiImplTest {
    @Mock
    MmeRestClient mmeRestClient;
    @InjectMocks
    MmeApiImpl mmeApi;

    @Test
    void trainModelTest() {
        ObjectNode jsonNode = new ObjectNode(JsonNodeFactory.instance);

        when(mmeRestClient.trainModel(any(JsonNode.class), anyString(), anyString(), anyString())).thenReturn(new MmeTrainModelResponse());

        assertThat(
                mmeApi.trainModel(
                        jsonNode,
                        UUID.randomUUID().toString(),
                        UUID.randomUUID().toString(),
                        UUID.randomUUID().toString()
                )
        ).isNotNull();
    }

    @Test
    void getTrainedModelTest() {
        MmeGetTrainedModelRequest trainRequest = new MmeGetTrainedModelRequest(UUID.randomUUID().toString(), false);

        when(mmeRestClient.getTrainedModel(any(MmeGetTrainedModelRequest.class), anyString(), anyString(), anyString(), anyString())).thenReturn(new MmeTrainModelResponse());

        assertThat(
                mmeApi.getTrainedModel(
                        trainRequest,
                        UUID.randomUUID().toString(),
                        UUID.randomUUID().toString(),
                        UUID.randomUUID().toString(),
                        UUID.randomUUID().toString()
                )
        ).isNotNull().isInstanceOf(MmeTrainModelResponse.class);
    }

}
