package com.quincus.finance.costing.common.exception;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@JsonRootName("apierror")
public class CostingApiError {

    private String status;
    private String message;
    private List<String> errors;
    private Instant timestamp = Instant.now();

    private String id;

}
