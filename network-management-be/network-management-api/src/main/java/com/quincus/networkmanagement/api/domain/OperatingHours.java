package com.quincus.networkmanagement.api.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.quincus.networkmanagement.api.constant.TimeUnit;
import com.quincus.networkmanagement.api.validator.constraint.ValidOperatingHours;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalTime;

import static com.quincus.networkmanagement.api.constant.TimeFormat.TWELVE_HOUR;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@ValidOperatingHours
public class OperatingHours extends Tenant {
    @JsonFormat(pattern = TWELVE_HOUR)
    private LocalTime monStartTime;
    @JsonFormat(pattern = TWELVE_HOUR)
    private LocalTime monEndTime;
    @Min(0)
    @Max(999999)
    private Integer monProcessingTime;
    @JsonFormat(pattern = TWELVE_HOUR)
    private LocalTime tueStartTime;
    @JsonFormat(pattern = TWELVE_HOUR)
    private LocalTime tueEndTime;
    @Min(0)
    @Max(999999)
    private Integer tueProcessingTime;
    @JsonFormat(pattern = TWELVE_HOUR)
    private LocalTime wedStartTime;
    @JsonFormat(pattern = TWELVE_HOUR)
    private LocalTime wedEndTime;
    @Min(0)
    @Max(999999)
    private Integer wedProcessingTime;
    @JsonFormat(pattern = TWELVE_HOUR)
    private LocalTime thuStartTime;
    @JsonFormat(pattern = TWELVE_HOUR)
    private LocalTime thuEndTime;
    @Min(0)
    @Max(999999)
    private Integer thuProcessingTime;
    @JsonFormat(pattern = TWELVE_HOUR)
    private LocalTime friStartTime;
    @JsonFormat(pattern = TWELVE_HOUR)
    private LocalTime friEndTime;
    @Min(0)
    @Max(999999)
    private Integer friProcessingTime;
    @JsonFormat(pattern = TWELVE_HOUR)
    private LocalTime satStartTime;
    @JsonFormat(pattern = TWELVE_HOUR)
    private LocalTime satEndTime;
    @Min(0)
    @Max(999999)
    private Integer satProcessingTime;
    @JsonFormat(pattern = TWELVE_HOUR)
    private LocalTime sunStartTime;
    @JsonFormat(pattern = TWELVE_HOUR)
    private LocalTime sunEndTime;
    @Min(0)
    @Max(999999)
    private Integer sunProcessingTime;
    @NotNull
    private TimeUnit processingTimeUnit;
    @Override
    @JsonIgnore
    public String getOrganizationId() {
        return super.getOrganizationId();
    }
}
