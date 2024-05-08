package com.quincus.shipment.impl.repository;

import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.impl.repository.constant.LocationQuery;
import com.quincus.shipment.impl.repository.entity.LocationEntity;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.Tuple;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface LocationHierarchyRepository extends JpaRepository<LocationHierarchyEntity, String> {
    @Query(value = "SELECT lh FROM LocationHierarchyEntity lh " +
            "WHERE lh.countryId = :countryId " +
            "AND   lh.stateId = :stateId " +
            "AND   lh.cityId = :cityId " +
            "AND   (:facilityId IS NULL OR lh.facilityId = :facilityId) " +
            "AND   lh.organizationId = :organizationId")
    Optional<LocationHierarchyEntity> findByCountryAndStateAndCityAndFacilityAndOrganizationId(String countryId, String stateId, String cityId, String facilityId, String organizationId);

    @Query(value = "SELECT lh FROM LocationHierarchyEntity lh " +
            "WHERE lh.countryId = :countryId " +
            "AND   lh.stateId = :stateId " +
            "AND   lh.cityId = :cityId " +
            "AND   lh.facilityId IS NULL " +
            "AND   lh.organizationId = :organizationId")
    Optional<LocationHierarchyEntity> findLHWithNullFacility(String countryId, String stateId, String cityId, String organizationId);

    //Alternative for findLHWithNullFacility as getting error on multiple result on query
    @Query(value = "SELECT lh FROM LocationHierarchyEntity lh where id = " +
            " ( SELECT max(lh.id) FROM LocationHierarchyEntity lh " +
            "WHERE lh.countryId = :countryId " +
            "AND   lh.stateId = :stateId " +
            "AND   lh.cityId = :cityId " +
            "AND   lh.facilityId IS NULL " +
            "AND   lh.organizationId = :organizationId)")
    Optional<LocationHierarchyEntity> findLHWithoutFacility(String countryId, String stateId, String cityId, String organizationId);

    @Query(value = "SELECT lh FROM LocationHierarchyEntity lh " +
            "WHERE (:countryId IS NULL OR lh.countryId = :countryId) " +
            "AND (:stateId IS NULL OR lh.stateId = :stateId) " +
            "AND (:cityId IS NULL OR lh.cityId = :cityId) " +
            "AND (lh.organizationId = :organizationId)")
    List<LocationHierarchyEntity> find(String countryId, String stateId, String cityId, String organizationId);

    @Query(value = "SELECT LOWER(lh.countryCode) as lhCountryCode, " +
            LocationQuery.COUNTRY_LOCATION_FIELD +
            " FROM LocationHierarchyEntity lh" +
            " LEFT JOIN lh.country country" +
            " WHERE (:key IS NULL OR LOWER(lh.countryCode) LIKE LOWER(concat('%', CONCAT(:key, '%'))) ) " +
            " AND lh.organizationId = :organizationId " +
            " AND lh.countryId IS NOT NULL " +
            " AND lh.countryCode IS NOT NULL" +
            " AND (:countryId IS NULL OR lh.countryId = :countryId) " +
            " GROUP BY LOWER(lh.countryCode), " +
            LocationQuery.COUNTRY_GROUP_BY_LOCATION_FIELD +
            " ORDER BY LOWER(lh.countryCode) ASC", countProjection = "DISTINCT lh.countryId")
    Page<Tuple> findCountries(String countryId, String key, String organizationId, Pageable page);


    @Query(value = "SELECT LOWER(lh.countryCode) as lhCountryCode, LOWER(lh.stateCode) as lhStateCode, " +
            LocationQuery.COUNTRY_LOCATION_FIELD +
            ", " + LocationQuery.STATE_LOCATION_FIELD +
            "FROM LocationHierarchyEntity lh " +
            "LEFT JOIN lh.country country " +
            "LEFT JOIN lh.state state " +
            "WHERE (:key IS NULL OR LOWER(lh.stateCode) LIKE LOWER(concat('%', CONCAT(:key, '%')))) " +
            "AND lh.organizationId = :organizationId " +
            "AND lh.country IS NOT NULL " +
            "AND lh.countryCode IS NOT NULL " +
            "AND lh.stateCode IS NOT NULL " +
            "AND lh.state IS NOT NULL " +
            "AND (:countryId IS NULL OR lh.countryId = :countryId) " +
            "AND (:stateId IS NULL OR lh.stateId = :stateId) " +
            "GROUP BY LOWER(lh.countryCode), LOWER(lh.stateCode), " +
            LocationQuery.COUNTRY_GROUP_BY_LOCATION_FIELD +
            ", " + LocationQuery.STATE_GROUP_BY_LOCATION_FIELD +
            "ORDER BY LOWER(lh.countryCode), LOWER(lh.stateCode) ASC", countProjection = "DISTINCT lh.stateId")
    Page<Tuple> findStates(String countryId, String stateId, String key, String organizationId, Pageable page);


    @Query(value = "SELECT  LOWER(lh.countryCode) as lhCountryCode, LOWER(lh.stateCode) as lhStateCode" +
            ", LOWER(lh.cityCode) as lhCityCode, " +
            LocationQuery.COUNTRY_LOCATION_FIELD +
            ", " + LocationQuery.STATE_LOCATION_FIELD +
            ", " + LocationQuery.CITY_LOCATION_FIELD +
            "FROM LocationHierarchyEntity lh " +
            "LEFT JOIN lh.country country " +
            "LEFT JOIN lh.state state " +
            "LEFT JOIN lh.city city " +
            "WHERE (:key IS NULL OR LOWER(lh.cityCode) LIKE LOWER(concat('%', CONCAT(:key, '%')))) " +
            "AND lh.organizationId = :organizationId " +
            "AND lh.country IS NOT NULL " +
            "AND lh.countryCode IS NOT NULL " +
            "AND lh.stateCode IS NOT NULL " +
            "AND lh.state IS NOT NULL " +
            "AND lh.cityCode IS NOT NULL " +
            "AND lh.city IS NOT NULL " +
            "AND (:countryId IS NULL OR lh.countryId = :countryId) " +
            "AND (:stateId IS NULL OR lh.stateId = :stateId) " +
            "AND (:cityId IS NULL OR lh.cityId = :cityId) " +
            "GROUP BY LOWER(lh.countryCode), LOWER(lh.stateCode), LOWER(lh.cityCode), " +
            LocationQuery.COUNTRY_GROUP_BY_LOCATION_FIELD +
            ", " + LocationQuery.STATE_GROUP_BY_LOCATION_FIELD +
            ", " + LocationQuery.CITY_GROUP_BY_LOCATION_FIELD +
            "ORDER BY LOWER(lh.countryCode), LOWER(lh.stateCode), LOWER(lh.cityCode) ASC", countProjection = "DISTINCT lh.cityId")
    Page<Tuple> findCities(String countryId, String stateId, String cityId, String key, String organizationId, Pageable page);


    @Query(value = "SELECT  LOWER(lh.countryCode) as lhCountryCode, LOWER(lh.stateCode) as lhStateCode" +
            ", LOWER(lh.cityCode) as lhCityCode, LOWER(lh.facilityCode) as lhFacilityCode, " +
            LocationQuery.COUNTRY_LOCATION_FIELD +
            ", " + LocationQuery.STATE_LOCATION_FIELD +
            ", " + LocationQuery.CITY_LOCATION_FIELD +
            ", " + LocationQuery.FACILITY_LOCATION_FIELD +
            "FROM LocationHierarchyEntity lh " +
            "LEFT JOIN lh.country country " +
            "LEFT JOIN lh.state state " +
            "LEFT JOIN lh.city city " +
            "LEFT JOIN lh.facility facility " +
            "WHERE (:key IS NULL OR LOWER(lh.facilityCode) LIKE LOWER(concat('%', CONCAT(:key, '%')))) " +
            "AND lh.organizationId = :organizationId " +
            "AND lh.country IS NOT NULL " +
            "AND lh.countryCode IS NOT NULL " +
            "AND lh.stateCode IS NOT NULL " +
            "AND lh.state IS NOT NULL " +
            "AND lh.cityCode IS NOT NULL " +
            "AND lh.city IS NOT NULL " +
            "AND lh.facilityCode IS NOT NULL " +
            "AND lh.facility IS NOT NULL " +
            "AND (:countryId IS NULL OR lh.countryId = :countryId) " +
            "AND (:stateId IS NULL OR lh.stateId = :stateId) " +
            "AND (:cityId IS NULL OR lh.cityId = :cityId) " +
            "AND (:facilityId IS NULL OR lh.facilityId = :facilityId) " +
            "AND lh.facilityCode != '" + Shipment.ORIGIN_PROPERTY_NAME + "' " +
            "AND lh.facilityCode != '" + Shipment.DESTINATION_PROPERTY_NAME + "'" +
            "GROUP BY LOWER(lh.countryCode), LOWER(lh.stateCode)" +
            ", LOWER(lh.cityCode), LOWER(lh.facilityCode), " +
            LocationQuery.COUNTRY_GROUP_BY_LOCATION_FIELD +
            ", " + LocationQuery.STATE_GROUP_BY_LOCATION_FIELD +
            ", " + LocationQuery.CITY_GROUP_BY_LOCATION_FIELD +
            ", " + LocationQuery.FACILITY_GROUP_BY_LOCATION_FIELD +
            "ORDER BY LOWER(lh.countryCode), LOWER(lh.stateCode), " +
            "LOWER(lh.cityCode), LOWER(lh.facilityCode) ASC", countProjection = "DISTINCT lh.facilityId")
    Page<Tuple> findFacilities(String countryId, String stateId, String cityId, String facilityId, String key, String organizationId, Pageable page);


    @Query(value = "SELECT DISTINCT lh.state FROM LocationHierarchyEntity lh " +
            "WHERE lh.countryId = :countryId " +
            "AND lh.organizationId = :organizationId")
    Page<LocationEntity> findStates(String countryId, String organizationId, Pageable page);

    @Query(value = "SELECT DISTINCT lh.city FROM LocationHierarchyEntity lh " +
            "WHERE lh.countryId = :countryId " +
            "AND lh.stateId = :stateId " +
            "AND lh.organizationId = :organizationId")
    Page<LocationEntity> findCities(String countryId, String stateId, String organizationId, Pageable page);

    @Query(value = "SELECT DISTINCT lh.facility FROM LocationHierarchyEntity lh " +
            "WHERE lh.countryId = :countryId " +
            "AND lh.stateId = :stateId " +
            "AND lh.cityId = :cityId " +
            "AND lh.organizationId = :organizationId " +
            "AND lh.facilityCode != '" + Shipment.ORIGIN_PROPERTY_NAME + "' " +
            "AND lh.facilityCode != '" + Shipment.DESTINATION_PROPERTY_NAME + "'")
    Page<LocationEntity> findFacilities(String countryId, String stateId, String cityId, String organizationId, Pageable page);

    @Query(value = "SELECT DISTINCT lh.id as id, lh.countryCode as countryCode, lh.stateCode as stateCode, lh.cityCode as cityCode, lh.facilityCode as facilityCode" +
            ", lh.facilityLocationCode as facilityLocationCode, lh.facilityName as facilityName" +
            ", country.id as countryLocId, country.externalId as countryLocExternalId, country.name as countryLocName, country.code as countryLocCode" +
            ", state.id as stateLocId, state.externalId as stateLocExternalId, state.name as stateLocName, state.code as stateLocCode" +
            ", city.id as cityLocId, city.externalId as cityLocExternalId, city.name as cityLocName, city.code as cityLocCode" +
            ", facility.id as facilityLocId, facility.externalId as facilityLocExternalId, facility.name as facilityLocName, facility.code as facilityLocCode" +
            " FROM LocationHierarchyEntity lh" +
            " INNER JOIN lh.country country" +
            " INNER JOIN lh.state state" +
            " INNER JOIN lh.city city" +
            " LEFT JOIN lh.facility facility" +
            " WHERE lh.id in(:ids) ")
    List<Tuple> findAllByIdsIn(Set<String> ids);

    @Query(value = "SELECT lh FROM LocationHierarchyEntity lh " +
            "WHERE lh.country.externalId = :extCountryId " +
            "AND lh.state.externalId = :extStateId " +
            "AND lh.city.externalId = :extCityId " +
            "AND (CASE WHEN :extFacilityId IS NULL THEN NULL ELSE lh.facility.externalId END) = :extFacilityId " +
            "AND lh.organizationId = :organizationId")
    LocationHierarchyEntity findByLocationExternalId(String extCountryId, String extCityId, String extStateId, String extFacilityId, String organizationId);

    @Query(value = "SELECT lh FROM LocationHierarchyEntity lh" +
            " JOIN FETCH lh.country country" +
            " JOIN FETCH lh.state state" +
            " JOIN FETCH lh.city city" +
            " JOIN FETCH lh.facility facility" +
            " WHERE facility.externalId in (:facilityExternalIds)" +
            " AND lh.organizationId = :currentOrganizationId")
    List<LocationHierarchyEntity> findByFacilityExternalIds(List<String> facilityExternalIds, String currentOrganizationId);
}