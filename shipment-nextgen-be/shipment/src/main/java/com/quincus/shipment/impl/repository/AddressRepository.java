package com.quincus.shipment.impl.repository;

import com.quincus.shipment.impl.repository.entity.AddressEntity;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<AddressEntity, String> {

    List<AddressEntity> findByLine1AndLine2AndLine3AndLocationHierarchy(String line1, String line2, String line3, LocationHierarchyEntity lh);

    @Query(value = "SELECT a.id, a.line1, a.line2, a.line3, lh.id, lh.countryCode, lh.stateCode, lh.cityCode " +
            "FROM AddressEntity a " +
            "LEFT JOIN a.locationHierarchy lh " +
            "WHERE a.id IN (:ids)")
    List<Object[]> findByIds(List<String> ids);

    AddressEntity findByLocationHierarchyId(String locationHierarchyId);

    @Query(value = "SELECT a FROM AddressEntity a WHERE a.locationHierarchyId in (:locationHierarchyIds)")
    List<AddressEntity> findByLocationHierarchyIds(List<String> locationHierarchyIds);
}
