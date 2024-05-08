package com.quincus.shipment.impl.service;

import com.quincus.shipment.impl.repository.AddressRepository;
import com.quincus.shipment.impl.repository.LocationHierarchyRepository;
import com.quincus.shipment.impl.repository.LocationRepository;
import com.quincus.shipment.impl.repository.ShipmentRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;

@Service
@AllArgsConstructor
public class CleanUpService {
    private final AddressRepository addressRepository;
    private final LocationRepository locationRepository;
    private final LocationHierarchyRepository locationHierarchyRepository;
    private final ShipmentRepository shipmentRepository;
    private Map<String, JpaRepository<?, String>> repositoryMap;

    @PostConstruct
    public void initEntityRepositoryMap() {
        repositoryMap = Map.of(
                "address", addressRepository,
                "location", locationRepository,
                "location_hierarchy", locationHierarchyRepository,
                "shipment", shipmentRepository);
    }

    public void cleanUpByEntityAndId(String entity, String id) {
        JpaRepository<?, String> entityRepository = repositoryMap.get(entity);
        if (entityRepository != null) {
            entityRepository.findById(id).ifPresent(e -> entityRepository.deleteById(id));
        }
    }

}
