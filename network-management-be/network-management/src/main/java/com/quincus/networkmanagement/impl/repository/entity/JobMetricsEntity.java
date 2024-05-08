package com.quincus.networkmanagement.impl.repository.entity;

import com.quincus.networkmanagement.api.constant.JobState;
import com.quincus.networkmanagement.impl.repository.entity.component.TenantEntity;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "job_metrics")
@ToString(onlyExplicitlyIncluded = true)
@TypeDef(name = "json", typeClass = JsonStringType.class)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class JobMetricsEntity extends TenantEntity {
    @Column(name = "status", length = 20)
    @Enumerated(EnumType.STRING)
    private JobState status;

    @Column(name = "data_with_error")
    @Type(type = "json")
    private String dataWithError;

    @Column(name = "fail_reason")
    private String failReason;

    @Column(name = "successful_records")
    private Long successfulRecords;

    @Column(name = "processed_records")
    private Long processedRecords;

    @Column(name = "failed_records")
    private Long failedRecords;

    @Column(name = "total_records")
    private Long totalRecords;

    @Column(name = "record_class_type")
    private String recordClassType;

    @Column(name = "executed_by_id")
    private String executedById;

    @Column(name = "executed_by_name")
    private String executedByName;

    @Column(name = "start_time")
    private Instant timeStarted;

    @Column(name = "time_elapsed_ms")
    private long timeElapsed;
}
