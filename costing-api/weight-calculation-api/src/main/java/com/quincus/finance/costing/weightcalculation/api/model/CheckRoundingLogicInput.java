package com.quincus.finance.costing.weightcalculation.api.model;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.quincus.finance.costing.common.validator.RoundingLogicConstraint;
import com.quincus.finance.costing.common.web.model.RoundingLogic;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonRootName("data")
public class CheckRoundingLogicInput {
    @NotNull
    private BigDecimal value;
    @NotNull
    @RoundingLogicConstraint
    private RoundingLogic roundingLogic;
}
