package com.quincus.networkmanagement.api.domain;

import com.quincus.networkmanagement.api.validator.constraint.ValidPartner;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Embeddable;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ValidPartner
public class Partner {
    private String id;
    private String name;
    private String code;
}
