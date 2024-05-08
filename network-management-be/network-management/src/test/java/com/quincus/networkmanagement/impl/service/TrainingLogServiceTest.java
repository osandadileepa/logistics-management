package com.quincus.networkmanagement.impl.service;

import com.quincus.networkmanagement.api.domain.TrainingLog;
import com.quincus.networkmanagement.impl.mapper.TrainingLogMapper;
import com.quincus.networkmanagement.impl.repository.TrainingLogRepository;
import com.quincus.networkmanagement.impl.repository.entity.TrainingLogEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import static com.quincus.networkmanagement.impl.data.PreprocessingTestData.dummyTrainingLog;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {MmeService.class})
class TrainingLogServiceTest {
    @Mock
    private TrainingLogRepository trainDataRepository;
    @Mock
    private TrainingLogMapper trainingLogMapper;
    @InjectMocks
    private TrainingLogService trainDataService;


    @Test
    void testCreateTrainingLog() {
        TrainingLog trainingLog = dummyTrainingLog();
        trainDataService.create(trainingLog);

        verify(trainDataRepository, times(1)).saveAndFlush(any());
    }

    @Test
    void testUpdateTrainingLog() {

        TrainingLog trainingLog = dummyTrainingLog();

        when(trainDataRepository.findByUniqueId(any())).thenReturn(Optional.of(new TrainingLogEntity()));
        trainDataService.update(trainingLog);

        verify(trainDataRepository, times(1)).findByUniqueId(trainingLog.getUniqueId());
        verify(trainDataRepository, times(1)).save(any());
    }

}

