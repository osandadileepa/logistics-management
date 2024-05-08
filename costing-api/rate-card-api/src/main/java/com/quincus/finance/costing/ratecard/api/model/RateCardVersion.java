package com.quincus.finance.costing.ratecard.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RateCardVersion {
    private String id;
    @NotNull
    @NotBlank
    private String name;
    @NotNull
    private RateCardType type;
    private List<RateCard> rateCards = new ArrayList<>();
}
