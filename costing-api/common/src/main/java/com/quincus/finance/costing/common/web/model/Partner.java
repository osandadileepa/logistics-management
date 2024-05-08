package com.quincus.finance.costing.common.web.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Partner {

    @NotNull
    @NotBlank
    private String id;
    @NotNull
    @NotBlank
    private String name;

}