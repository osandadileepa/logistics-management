package com.quincus.shipment.impl.repository;

import com.quincus.shipment.impl.repository.entity.AlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.Tuple;
import java.util.Collection;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<AlertEntity, String> {

    @Query(value = """
            SELECT
              al.id as id, al.shortMessage as shortMessage , al.message as message, al.type as type, al.level as level,
              al.constraint as constraint, al.dismissed as dismissed, al.dismissTime as dismissTime,
              al.dismissedBy as dismissedBy, al.shipmentJourneyId as shipmentJourneyId,
              al.packageJourneySegmentId as packageJourneySegmentId,
              al.fields as fields
            FROM
              AlertEntity al
            WHERE
              al.packageJourneySegmentId in (:segmentIds)
            OR
              al.shipmentJourneyId in (:journeyIds)
            """)
    List<Tuple> findByJourneyIdsAndSegmentIds(List<String> journeyIds, List<String> segmentIds);

    @Query(value = """
                SELECT COUNT(alert) > 0
                FROM AlertEntity alert
                WHERE alert.id = :alertId
                AND (
                    EXISTS (
                        SELECT 1 FROM ShipmentJourneyEntity journey
                        INNER JOIN journey.packageJourneySegments segment
                        INNER JOIN segment.startLocationHierarchy startFacility
                        INNER JOIN startFacility.city startCity
                        INNER JOIN startFacility.state startState
                        INNER JOIN startFacility.country startCountry
                        INNER JOIN segment.endLocationHierarchy endFacility
                        INNER JOIN endFacility.city endCity
                        INNER JOIN endFacility.state endState
                        INNER JOIN endFacility.country endCountry
                        WHERE alert.shipmentJourneyId = journey.id
                        AND (
                            startFacility.externalId IN (:facilityIdsCoverage)
                            OR startCity.externalId IN (:cityIdsCoverage)
                            OR startState.externalId IN (:stateIdsCoverage)
                            OR startCountry.externalId IN (:countryIdsCoverage)
                            OR endFacility.externalId IN (:facilityIdsCoverage)
                            OR endCity.externalId IN (:cityIdsCoverage)
                            OR endState.externalId IN (:stateIdsCoverage)
                            OR endCountry.externalId IN (:countryIdsCoverage)
                        )
                    )
                    OR EXISTS (
                        SELECT 1 FROM PackageJourneySegmentEntity segment
                        INNER JOIN segment.startLocationHierarchy startFacility
                        INNER JOIN startFacility.city startCity
                        INNER JOIN startFacility.state startState
                        INNER JOIN startFacility.country startCountry
                        INNER JOIN segment.endLocationHierarchy endFacility
                        INNER JOIN endFacility.city endCity
                        INNER JOIN endFacility.state endState
                        INNER JOIN endFacility.country endCountry
                        WHERE alert.packageJourneySegmentId = segment.id
                        AND (
                            startFacility.externalId IN (:facilityIdsCoverage)
                            OR startCity.externalId IN (:cityIdsCoverage)
                            OR startState.externalId IN (:stateIdsCoverage)
                            OR startCountry.externalId IN (:countryIdsCoverage)
                            OR endFacility.externalId IN (:facilityIdsCoverage)
                            OR endCity.externalId IN (:cityIdsCoverage)
                            OR endState.externalId IN (:stateIdsCoverage)
                            OR endCountry.externalId IN (:countryIdsCoverage)
                        )
                    )
                )
            """)
    boolean isAlertFromSegmentLocationCovered(String alertId, Collection<String> facilityIdsCoverage,
                                              Collection<String> cityIdsCoverage, Collection<String> stateIdsCoverage,
                                              Collection<String> countryIdsCoverage);
}
