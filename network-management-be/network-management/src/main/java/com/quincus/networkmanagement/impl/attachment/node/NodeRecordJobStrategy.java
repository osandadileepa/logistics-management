package com.quincus.networkmanagement.impl.attachment.node;

import com.quincus.networkmanagement.api.domain.Node;
import com.quincus.networkmanagement.api.exception.JobRecordExecutionException;
import com.quincus.networkmanagement.impl.attachment.JobTemplateStrategy;
import com.quincus.networkmanagement.impl.service.JobMetricsService;
import com.quincus.networkmanagement.impl.service.NodeService;
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
public class NodeRecordJobStrategy extends JobTemplateStrategy<NodeRecord> {
    private final NodeService service;
    private final NodeRecordMapper mapper;
    private final Validator validator;

    public NodeRecordJobStrategy(
            JobMetricsService<NodeRecord> jobMetricsService,
            NodeService service,
            NodeRecordMapper mapper,
            Validator validator
    ) {
        super(jobMetricsService);
        this.service = service;
        this.mapper = mapper;
        this.validator = validator;
    }

    @Override
    @Transactional(noRollbackFor = JobRecordExecutionException.class)
    public void execute(NodeRecord connectionRecord, boolean overwrite) throws JobRecordExecutionException {
        log.info("Processing node record from uploaded file: {}", connectionRecord);
        validateThenSave(connectionRecord, overwrite);
    }

    private void validateThenSave(NodeRecord nodeRecord, boolean overwrite) {
        try {
            Node node = mapper.toDomain(nodeRecord);

            Set<ConstraintViolation<Node>> violations = validator.validate(node);
            if (!violations.isEmpty()) {
                setFailedReasonAndThrowException(
                        nodeRecord,
                        violations.stream().map(
                                violation ->
                                        StringUtils.isNotBlank(violation.getPropertyPath().toString()) ?
                                                violation.getPropertyPath().toString() + " " + violation.getMessage() :
                                                violation.getMessage()
                        ).toList()
                );
            } else {
                if (overwrite) {
                    log.debug("Attempting to create or update node: `{}` from record: `{}`", node, nodeRecord);
                    service.createOrUpdateWithNoRollback(node);
                } else {
                    log.debug("Attempting to create node: `{}` from record: `{}`", node, nodeRecord);
                    service.createWithNoRollback(node);
                }
            }
        } catch (QuincusException e) {
            log.debug("Failed to process one node record: {} due to: {}", nodeRecord, e.getMessage());
            setFailedReasonAndThrowException(
                    nodeRecord,
                    e.getMessage()
            );
            throw new JobRecordExecutionException(e.getMessage());
        }
    }

    private void setFailedReasonAndThrowException(NodeRecord nodeRecord, List<String> errorMessages) {
        if (CollectionUtils.isEmpty(errorMessages)) {
            return;
        }
        String lineErrorMessage = errorMessages.toString();
        nodeRecord.setFailedReason(lineErrorMessage);
        throw new JobRecordExecutionException(lineErrorMessage);
    }

    private void setFailedReasonAndThrowException(NodeRecord nodeRecord, String errorMessage) {
        nodeRecord.setFailedReason(errorMessage);
        throw new JobRecordExecutionException(errorMessage);
    }
}
