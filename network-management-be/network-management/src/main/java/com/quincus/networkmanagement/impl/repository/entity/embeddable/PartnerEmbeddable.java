package com.quincus.networkmanagement.impl.repository.entity.embeddable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Embeddable;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PartnerEmbeddable {
    private String id;
    private String name;
    private String code;
}
