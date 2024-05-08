package com.quincus.shipment.api.domain;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MilestoneError {
    private List<String> errors;
    private JsonNode milestone;
}
