package com.quincus.shipment.impl.repository;

import com.quincus.shipment.impl.repository.entity.NetworkLaneSegmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.Tuple;
import java.util.List;

@Repository
public interface NetworkLaneSegmentRepository extends JpaRepository<NetworkLaneSegmentEntity, String> {

    @Query(value = """ 
            SELECT nls.id as id, nls.transportType as transportType, nls.sequence as sequence,
                       nls.vehicleInfo as vehicleInfo, nls.flightNumber as flightNumber,
                       nls.airline as airline, nls.airlineCode as airlineCode, nls.masterWaybill as masterWaybill,
                       nls.pickupInstruction as pickupInstruction, nls.deliveryInstruction as deliveryInstruction,
                       nls.duration as duration, nls.durationUnit as durationUnit, nls.pickUpTime as pickUpTime, nls.pickUpTimezone as pickUpTimezone,
                       nls.dropOffTime as dropOffTime, nls.dropOffTimezone as dropOffTimezone, nls.lockOutTime as lockOutTime, nls.lockOutTimezone as lockOutTimezone,
                       nls.departureTime as departureTime, nls.departureTimezone as departureTimezone, nls.arrivalTime as arrivalTime, nls.arrivalTimezone as arrivalTimezone, 
                       nls.recoveryTime as recoveryTime, nls.recoveryTimezone as recoveryTimezone,
                       nls.calculatedMileage as calculatedMileage, nls.calculatedMileageUnit as calculatedMileageUnit,
                       nls.networkLaneId as networkLaneId, nls.organizationId as organizationId, partner.id as partnerId, partner.name as partnerName,
                       partner.code as partnerCode, partner.partnerType as partnerType, partner.externalId as partnerExternalId,
                       startLH.id as startLHId, startLH.countryCode as startLHCountryCode, startLH.stateCode as startLHStateCode,
                       startLH.cityCode as startLHCityCode, startLH.facilityCode as startLHFacilityCode,
                       startLH.facilityLocationCode as startLHFacilityLocationCode, startCountry.id as startCountryId,
                       startCountry.externalId as startCountryExternalId, startCountry.name as startCountryName,
                       startState.id as startStateId, startState.externalId as startStateExternalId, startState.name as startStateName,
                       startCity.id as startCityId, startCity.externalId as startCityExternalId, startCity.name as startCityName,
                       startFacility.id as startFacilityId, startFacility.externalId as startFacilityExternalId, startFacility.name as startFacilityName,
                       endLH.id as endLHId, endLH.countryCode as endLHCountryCode, endLH.stateCode as endLHStateCode,
                       endLH.cityCode as endLHCityCode ,endLH.facilityCode as endLHFacilityCode, endLH.facilityLocationCode as endLHFacilityLocationCode,
                       endCountry.id as endCountryId, endCountry.externalId as endCountryExternalId, endCountry.name as endCountryName,
                       endState.id as endStateId, endState.externalId as endStateExternalId, endState.name as endStateName,
                       endCity.id as endCityId, endCity.externalId as endCityExternalId, endCity.name as endCityName,
                       endFacility.id as endFacilityId, endFacility.externalId as endFacilityExternalId, endFacility.name as endFacilityName
                FROM NetworkLaneSegmentEntity nls
                LEFT JOIN nls.partner partner
                LEFT JOIN nls.startLocationHierarchy startLH
                LEFT JOIN startLH.country startCountry
                LEFT JOIN startLH.state startState
                LEFT JOIN startLH.city startCity
                LEFT JOIN startLH.facility startFacility
                LEFT JOIN nls.endLocationHierarchy endLH
                LEFT JOIN endLH.country endCountry
                LEFT JOIN endLH.state endState
                LEFT JOIN endLH.city endCity
                LEFT JOIN endLH.facility endFacility
                WHERE nls.networkLaneId in (:ids)""")
    List<Tuple> findAllByNetworkLaneIdsIn(List<String> ids);
}