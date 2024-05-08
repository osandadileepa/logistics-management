package com.quincus.shipment.impl.repository.listeners;

import com.quincus.shipment.impl.repository.entity.component.IdentifiableEntity;

import javax.persistence.PrePersist;
import java.util.UUID;

public class BaseEntityListener {

    @PrePersist
    public void prePersist(IdentifiableEntity entity) {
        if (entity.shouldGenerateId()) {
            entity.setId(UUID.randomUUID().toString());
        }
    }
}
