package com.quincus.networkmanagement.impl.repository.entity.embeddable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Embeddable;

@Getter
@Setter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class NodeEmbeddable {
    private String id;
    private String nodeCode;
}
