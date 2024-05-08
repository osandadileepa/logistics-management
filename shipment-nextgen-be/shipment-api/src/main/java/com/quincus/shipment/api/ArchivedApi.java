package com.quincus.shipment.api;

import com.quincus.shipment.api.domain.Archived;

import java.util.Optional;

public interface ArchivedApi {

    void save(Archived archive);

    Optional<Archived> findLatestByReferenceId(String referenceId);
}
