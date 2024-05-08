package com.quincus.networkmanagement.impl.attachment.connection;

import com.quincus.networkmanagement.api.domain.Connection;
import com.quincus.networkmanagement.api.exception.JobRecordExecutionException;
import com.quincus.networkmanagement.impl.attachment.JobTemplateStrategy;
import com.quincus.networkmanagement.impl.service.ConnectionService;
import com.quincus.networkmanagement.impl.service.JobMetricsService;
import com.quincus.web.common.exception.model.QuincusException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class ConnectionRecordJobStrategy extends JobTemplateStrategy<ConnectionRecord> {
    private final ConnectionService service;
    private final ConnectionRecordMapper mapper;
    private final Validator validator;

    public ConnectionRecordJobStrategy(
            JobMetricsService<ConnectionRecord> jobMetricsService,
            ConnectionService service,
            ConnectionRecordMapper mapper,
            Validator validator
    ) {
        super(jobMetricsService);
        this.service = service;
        this.mapper = mapper;
        this.validator = validator;
    }

    @Override
    @Transactional(noRollbackFor = JobRecordExecutionException.class)
    public void execute(ConnectionRecord connectionRecord, boolean overwrite) throws JobRecordExecutionException {
        log.debug("Processing connection record from uploaded file: `{}`", connectionRecord);
        validateThenSave(connectionRecord, overwrite);
    }

    private void validateThenSave(ConnectionRecord connectionRecord, boolean overwrite) {
        try {
            Connection connection = mapper.toDomain(connectionRecord);

            Set<ConstraintViolation<Connection>> violations = validator.validate(connection);
            if (!violations.isEmpty()) {
                setFailedReasonAndThrowException(
                        connectionRecord,
                        violations.stream().map(
                                violation ->
                                        StringUtils.isNotBlank(violation.getPropertyPath().toString()) ?
                                                violation.getPropertyPath().toString() + " " + violation.getMessage() :
                                                violation.getMessage()
                        ).toList()
                );
            } else {
                if (overwrite) {
                    log.debug("Attempting to create or update connection: `{}` from record: `{}`", connection, connectionRecord);
                    service.createOrUpdateWithNoRollback(connection);
                } else {
                    log.debug("Attempting to create connection: `{}` from record: `{}`", connection, connectionRecord);
                    service.createWithNoRollback(connection);
                }
            }
        } catch (QuincusException e) {
            log.debug("Failed to process one connection record: {} due to: {}", connectionRecord, e.getMessage());
            setFailedReasonAndThrowException(
                    connectionRecord,
                    e.getMessage()
            );
            throw new JobRecordExecutionException(e.getMessage());
        }
    }

    private void setFailedReasonAndThrowException(ConnectionRecord connectionRecord, List<String> errorMessages) {
        if (CollectionUtils.isEmpty(errorMessages)) {
            return;
        }
        String lineErrorMessage = errorMessages.toString();
        connectionRecord.setFailedReason(lineErrorMessage);
        throw new JobRecordExecutionException(lineErrorMessage);
    }

    private void setFailedReasonAndThrowException(ConnectionRecord connectionRecord, String errorMessage) {
        connectionRecord.setFailedReason(errorMessage);
        throw new JobRecordExecutionException(errorMessage);
    }
}
