package com.quincus.networkmanagement.impl.repository.entity;

import com.quincus.networkmanagement.api.constant.TrainingStatus;
import com.quincus.networkmanagement.api.constant.TrainingType;
import com.quincus.networkmanagement.impl.repository.entity.component.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.time.Instant;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Table(name = "training_log")
@FilterDef(name = "organizationFilter", parameters = @ParamDef(name = "organizationId", type = "string"))
@Filter(name = "organizationFilter", condition = "organization_id = :organizationId")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class TrainingLogEntity extends BaseEntity {
    @Column(name = "organization_id", nullable = false)
    private String organizationId;
    @Column(name = "user_id")
    private String userId; // user who initiated the training
    @Column(name = "training_request_id")
    private String trainingRequestId; // unique id generated per request
    @Column(name = "unique_id")
    private String uniqueId; // unique id sent by MME
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private TrainingStatus status; // current training status
    @Column(name = "start_time")
    private Instant timeStarted;
    @Column(name = "complete_time")
    private Instant timeCompleted;
    @Column(name = "generate_request_elapsed_time")
    private long generateRequestElapsedTime; // time it took for NW to generate the training input
    @Column(name = "training_type")
    @Enumerated(EnumType.STRING)
    private TrainingType trainingType;
}
