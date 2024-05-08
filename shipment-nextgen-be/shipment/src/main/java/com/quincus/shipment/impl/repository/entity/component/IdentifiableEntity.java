package com.quincus.shipment.impl.repository.entity.component;

public interface IdentifiableEntity {

    String getId();

    void setId(String id);

    default boolean shouldGenerateId() {
        return true;
    }
}
