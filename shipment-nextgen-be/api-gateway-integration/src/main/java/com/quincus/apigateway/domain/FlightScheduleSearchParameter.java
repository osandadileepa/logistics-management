package com.quincus.apigateway.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data
public class FlightScheduleSearchParameter {
    @NotBlank
    @Size(min = 1, max = 3)
    private String origin;
    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate departureDate;
    @NotBlank
    @Size(min = 1, max = 3)
    private String destination;

    @Size(max = 3)
    private String carrier;
}
