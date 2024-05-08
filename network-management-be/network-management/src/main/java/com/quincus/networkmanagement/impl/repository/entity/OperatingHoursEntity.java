package com.quincus.networkmanagement.impl.repository.entity;

import com.quincus.networkmanagement.api.constant.TimeUnit;
import com.quincus.networkmanagement.impl.repository.entity.component.TenantEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.time.LocalTime;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Entity
@Table(name = "operating_hours")
public class OperatingHoursEntity extends TenantEntity {
    @Column(name = "mon_start_time")
    private LocalTime monStartTime;
    @Column(name = "mon_end_time")
    private LocalTime monEndTime;
    @Column(name = "mon_processing_time")
    private Integer monProcessingTime;
    @Column(name = "tue_start_time")
    private LocalTime tueStartTime;
    @Column(name = "tue_end_time")
    private LocalTime tueEndTime;
    @Column(name = "tue_processing_time")
    private Integer tueProcessingTime;
    @Column(name = "wed_start_time")
    private LocalTime wedStartTime;
    @Column(name = "wed_end_time")
    private LocalTime wedEndTime;
    @Column(name = "wed_processing_time")
    private Integer wedProcessingTime;
    @Column(name = "thu_start_time")
    private LocalTime thuStartTime;
    @Column(name = "thu_end_time")
    private LocalTime thuEndTime;
    @Column(name = "thu_processing_time")
    private Integer thuProcessingTime;
    @Column(name = "fri_start_time")
    private LocalTime friStartTime;
    @Column(name = "fri_end_time")
    private LocalTime friEndTime;
    @Column(name = "fri_processing_time")
    private Integer friProcessingTime;
    @Column(name = "sat_start_time")
    private LocalTime satStartTime;
    @Column(name = "sat_end_time")
    private LocalTime satEndTime;
    @Column(name = "sat_processing_time")
    private Integer satProcessingTime;
    @Column(name = "sun_start_time")
    private LocalTime sunStartTime;
    @Column(name = "sun_end_time")
    private LocalTime sunEndTime;
    @Column(name = "sun_processing_time")
    private Integer sunProcessingTime;
    @Column(name = "processing_time_unit")
    @Enumerated(EnumType.STRING)
    private TimeUnit processingTimeUnit;
}
