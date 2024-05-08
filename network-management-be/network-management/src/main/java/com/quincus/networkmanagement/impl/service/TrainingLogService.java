package com.quincus.networkmanagement.impl.service;

import com.quincus.networkmanagement.api.domain.TrainingLog;
import com.quincus.networkmanagement.api.exception.TrainingLogNotFoundException;
import com.quincus.networkmanagement.impl.mapper.TrainingLogMapper;
import com.quincus.networkmanagement.impl.repository.TrainingLogRepository;
import com.quincus.networkmanagement.impl.repository.entity.TrainingLogEntity;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class TrainingLogService {
    private final TrainingLogRepository repository;
    private final TrainingLogMapper mapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TrainingLog create(TrainingLog trainingLog) {
        return mapper.toDomain(repository.saveAndFlush(mapper.toEntity(trainingLog)));
    }

    @Transactional
    public TrainingLog update(TrainingLog trainingLog) {
        TrainingLogEntity trainingLogEntity = findByUniqueIdOrThrow(trainingLog.getUniqueId());
        return mapper.toDomain(repository.save(mapper.update(trainingLog, trainingLogEntity)));
    }

    public TrainingLog findByUniqueId(String uniqueId) {
        return mapper.toDomain(findByUniqueIdOrThrow(uniqueId));
    }

    private TrainingLogEntity findByUniqueIdOrThrow(String uniqueId) {
        return repository.findByUniqueId(uniqueId)
                .orElseThrow(() -> new TrainingLogNotFoundException(String.format("Training log with unique id `%s` not found", uniqueId)));
    }

    public Page<TrainingLog> list(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDomain);
    }
}
