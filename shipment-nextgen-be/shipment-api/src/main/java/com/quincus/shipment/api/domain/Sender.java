package com.quincus.shipment.api.domain;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sender {
    @NotBlank
    @Size(max=128, message = "Must be maximum of 128 characters.")
    private String name;
    @Size(max=64, message = "Must be maximum of 64 characters.")
    private String email;
    @Size(max=64, message = "Must be maximum of 64 characters.")
    private String contactNumber;
    @Size(max=64, message = "Must be maximum of 64 characters.")
    private String contactCode;
}
