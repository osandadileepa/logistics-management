package com.quincus.networkmanagement.impl.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quincus.mme.model.MmeGetTrainedModelRequest;
import com.quincus.networkmanagement.api.constant.TrainingType;
import com.quincus.networkmanagement.api.domain.TrainingLog;
import com.quincus.networkmanagement.impl.preprocessor.PreprocessingService;
import com.quincus.networkmanagement.impl.service.MmeService;
import com.quincus.web.common.multitenant.UserDetailsContextHolder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.quincus.networkmanagement.impl.data.PreprocessingTestData.dummyTrainingLog;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UtilityControllerImplTest {
    @Mock
    private PreprocessingService preprocessingService;

    @Mock
    private MmeService mmeService;
    @Mock
    private UserDetailsContextHolder userDetailsContextHolder;

    @InjectMocks
    private UtilityControllerImpl utilityController;

    @Test
    void testGenerateTrainingInput() {
        ObjectNode mockJsonNode = Mockito.mock(ObjectNode.class);
        String organizationId = "ORGANIZATION-ID";
        when(userDetailsContextHolder.getCurrentOrganizationId()).thenReturn(organizationId);
        when(preprocessingService.generateTrainingInput(organizationId, null)).thenReturn(mockJsonNode);

        JsonNode response = utilityController.generateTrainingInput(null);

        assertThat(response).isNotNull().isEqualTo(mockJsonNode);

        verify(preprocessingService).generateTrainingInput(organizationId, null);
    }

    @Test
    void testTrainModel() {
        TrainingLog trainingLog = dummyTrainingLog();
        String organizationId = "ORGANIZATION-ID";
        String organizationName = "ORGANIZATION-NAME";
        String userId = "USER-ID";
        when(userDetailsContextHolder.getCurrentOrganizationId()).thenReturn(organizationId);
        when(userDetailsContextHolder.getCurrentOrganizationName()).thenReturn(organizationName);
        when(userDetailsContextHolder.getCurrentUserId()).thenReturn(userId);

        when(mmeService.trainModel(TrainingType.MANUAL_VIA_UTILITY, organizationId, organizationName, userId)).thenReturn(trainingLog);

        TrainingLog response = utilityController.trainModel();

        assertThat(response).isNotNull().isEqualTo(trainingLog);

        verify(mmeService).trainModel(TrainingType.MANUAL_VIA_UTILITY, organizationId, organizationName, userId);
    }

    @Test
    void testGetTrainedModel() {
        MmeGetTrainedModelRequest trainRequest = new MmeGetTrainedModelRequest();
        TrainingLog trainingLog = dummyTrainingLog();

        when(mmeService.getTrainedModel(trainRequest)).thenReturn(trainingLog);
        TrainingLog response = utilityController.getTrainedModel(trainRequest);

        verify(mmeService).getTrainedModel(trainRequest);

        assertThat(response).isNotNull().isEqualTo(trainingLog);
    }
}

