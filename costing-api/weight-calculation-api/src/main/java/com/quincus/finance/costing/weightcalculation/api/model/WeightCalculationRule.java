package com.quincus.finance.costing.weightcalculation.api.model;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.quincus.finance.costing.common.validator.RoundingLogicConstraint;
import com.quincus.finance.costing.common.web.model.Partner;
import com.quincus.finance.costing.common.web.model.RoundingLogic;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonRootName("data")
public class WeightCalculationRule {

    private String id;
    private Instant createTime;
    private Instant modifyTime;
    @NotNull
    @NotBlank
    private String name;
    private String description;
    private boolean active;
    @NotNull
    private ChargeableWeightRule chargeableWeightRule;
    private BigDecimal chargeableWeightMin;
    private BigDecimal chargeableWeightMax;
    private VolumeWeightRule volumeWeightRule;
    private BigDecimal standardVolumeWeightRuleDivisor;
    private BigDecimal volumeWeightMin;
    private BigDecimal volumeWeightMax;
    private BigDecimal actualWeightMin;
    private BigDecimal actualWeightMax;
    private boolean actualWeightRounding;
    private boolean volumeWeightRounding;
    private boolean chargeableWeightRounding;
    @NotNull
    @RoundingLogicConstraint
    private RoundingLogic roundingLogic;
    @NotNull
    @NotBlank
    private String organizationId;
    @Valid
    private Set<Partner> partners = new HashSet<>();
    private SpecialVolumeWeightRule specialVolumeWeightRule;
    private String author = "Peter Parker";
}


