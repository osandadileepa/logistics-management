package com.quincus.shipment.impl.repository;

import com.quincus.shipment.api.constant.ShipmentStatus;
import com.quincus.shipment.impl.repository.constant.ShipmentQuery;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.Tuple;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<ShipmentEntity, String>, JpaSpecificationExecutor<ShipmentEntity> {

    String SELECT_FROM_SHIPMENT = "SELECT * FROM shipment s " +
            "LEFT JOIN service_type st on st.id = s.service_id " +
            "LEFT JOIN address o on o.id = s.origin " +
            "LEFT JOIN address d on d.id = s.destination " +
            "LEFT JOIN package p on p.id = s.package_id " +
            "LEFT JOIN (shipment_journey sj " +
            "LEFT JOIN package_journey_segment pjs on pjs.shipmentjourney_id = sj.id AND pjs.version = sj.version ) " +
            "on sj.id = s.shipment_journey_id " +
            "LEFT JOIN organization org on org.id = s.organization_id " +
            "LEFT JOIN shipment_order so on so.id = s.order_id " +
            "LEFT JOIN customer c on c.id = s.customer_id " +
            "LEFT JOIN partner pr on pr.id = s.partner_id ";

    @Query(nativeQuery = true,
            value = SELECT_FROM_SHIPMENT +
                    "WHERE s.shipment_tracking_id = :shipmentTrackingId " +
                    "AND s.organization_id = :organizationId")
    Optional<ShipmentEntity> findByShipmentTrackingIdAndOrgId(String shipmentTrackingId, String organizationId);

    @Query(value = """
            SELECT
              shp.id as id,
              shp.orderId as orderId,
              shp.shipmentJourney as shipmentJourney,
              shp.shipmentTrackingId as shipmentTrackingId,
              shp.status as status,
              shp.partnerId as partnerId,
              shp.order.pickupTimezone as pickupTimezone,
              shp.order.deliveryTimezone as deliveryTimezone
            FROM ShipmentEntity shp
            WHERE shp.orderId = :orderId
              AND shp.organization.id = :organizationId
            """)
    List<Tuple> findShipmentsPartialFieldByOrderId(String orderId, String organizationId);

    @Query(nativeQuery = true,
            value = SELECT_FROM_SHIPMENT +
                    "WHERE s.id = :id " +
                    "AND s.organization_id = :organizationId")
    Optional<ShipmentEntity> findById(String id, String organizationId);

    @Query(value = """
            SELECT shp
            FROM ShipmentEntity shp
            """ + ShipmentQuery.SHIPMENT_TO_SEGMENT_JOIN_FETCH + """
                    WHERE shp.id = :id
                  AND organization.id = :organizationId
            """)
    Optional<ShipmentEntity> findByIdWithFetch(String id, String organizationId);

    @Query(value = """
            SELECT shp
            FROM ShipmentEntity shp
            """ + ShipmentQuery.SHIPMENT_TO_COMMODITY_JOIN_FETCH + """
                    WHERE shp.id = :id
                  AND organization.id = :organizationId
            """)
    Optional<ShipmentEntity> findByIdAfterJourneyUpdate(String id, String organizationId);

    @Override
    List<ShipmentEntity> findAllById(Iterable<String> ids);


    @Override
    long count(Specification specs);

    @Query(value = """
            SELECT shp
            FROM ShipmentEntity shp
            """
            + ShipmentQuery.SHIPMENT_TO_SEGMENT_JOIN_FETCH +
            """
                    WHERE shp.shipmentTrackingId = :shipmentTrackingId AND shp.organization.id = :organizationId
                    """)
    Optional<ShipmentEntity> findByShipmentTrackingIdAndOrganizationId(String shipmentTrackingId, String organizationId);

    @Query(value = "SELECT shp FROM ShipmentEntity shp " +
            ShipmentQuery.SHIPMENT_JOIN_FETCH +
            " WHERE shp.shipmentJourney.id = :journeyId")
    List<ShipmentEntity> findByJourneyId(String journeyId);

    @Query(value = "SELECT s FROM ShipmentEntity s " +
            "LEFT JOIN OrderEntity o ON o.id = s.order.id " +
            "WHERE s.organization.id = :organizationId " +
            "  AND (s.externalOrderId = :orderNumber " +
            "       OR o.orderIdLabel = :orderNumber) ")
    List<ShipmentEntity> findByOrderNumberAndOrganizationId(String orderNumber, String organizationId);

    @Query(nativeQuery = true,
            value = """
                    SELECT
                      DISTINCT shp.id as shipment_id,
                      shp.shipment_tracking_id as shipment_tracking_id,
                      shp.organization_id as organization_id,
                      shp.order_id as order_id,
                      shp.shipment_journey_id as journey_id
                    FROM
                      shipment shp
                      INNER JOIN shipment_journey journey1 on shp.shipment_journey_id = journey1.id
                      INNER JOIN package_journey_segment segment1 on journey1.id = segment1.shipmentJourney_id
                      AND (segment1.deleted = 'false')
                    WHERE (shp.status NOT IN ('COMPLETED', 'CANCELLED'))
                      AND segment1.transport_type = 'AIR'
                      AND segment1.status = 'PLANNED'
                      AND (
                        segment1.id NOT IN (
                          SELECT
                            seg_lt_passed.segment_id
                          FROM
                            segment_lockout_time_passed seg_lt_passed
                        )
                      )
                      AND NOT (
                        EXISTS (
                          SELECT
                            a.package_journey_segment_id
                          FROM
                            alert a CROSS JOIN package_journey_segment segment2
                          WHERE
                            a.package_journey_segment_id = segment2.id
                            AND (a.type in ('ERROR', 'JOURNEY_REVIEW_REQUIRED'))
                            AND a.package_journey_segment_id = segment1.id
                        )
                      )
                    """)
    List<Tuple> findActiveShipmentsPartialFieldsWithAirSegmentAndSegmentUncachedAndSegmentNoAlert();

    @Query(nativeQuery = true,
            value = """
                    SELECT
                      DISTINCT shp.id as shipment_id,
                      shp.shipment_tracking_id as shipment_tracking_id,
                      shp.organization_id as organization_id,
                      shp.order_id as order_id,
                      shp.shipment_journey_id as journey_id
                    FROM
                      shipment shp
                      INNER JOIN shipment_journey journey1 on shp.shipment_journey_id = journey1.id
                      INNER JOIN package_journey_segment segment1 on journey1.id = segment1.shipmentJourney_id
                    WHERE (segment1.id IN (:segmentIdList)) AND (segment1.deleted = 'false')
                    """)
    List<Tuple> findShipmentsPartialFieldsFromSegmentIdList(@Param("segmentIdList") List<String> segmentIds);

    @Query(value = """
            SELECT
              shp
            FROM ShipmentEntity shp
              LEFT JOIN shp.shipmentJourney journey
              LEFT JOIN journey.packageJourneySegments segment
            WHERE shp.shipmentTrackingId = :shipmentTrackingId
              AND shp.organization.id = :organizationId
              AND segment.status IN ('PLANNED','IN_PROGRESS')
            """)
    Optional<ShipmentEntity> findShipmentActiveSegmentsOnlyByShipmentTrackingIdAndOrganizationId(String shipmentTrackingId, String organizationId);

    @Query(value = """
                SELECT COUNT(shp) > 0
                FROM ShipmentEntity shp
                JOIN ShipmentJourneyEntity journey ON shp.shipmentJourney.id = journey.id
                JOIN PackageJourneySegmentEntity packageJourneySegment ON journey.id = packageJourneySegment.shipmentJourneyId
                JOIN LocationHierarchyEntity locationHierarchy ON locationHierarchy.id IN (packageJourneySegment.startLocationHierarchy, packageJourneySegment.endLocationHierarchy)
                WHERE shp.id = :shipmentId
                    AND (
                        locationHierarchy.externalId IN (:facilityIdsCoverage)
                        OR locationHierarchy.city.externalId IN (:cityIdsCoverage)
                        OR locationHierarchy.state.externalId IN (:stateIdsCoverage)
                        OR locationHierarchy.country.externalId IN (:countryIdsCoverage)
                      )
            """)
    boolean isShipmentIdAnySegmentLocationCovered(
            final String shipmentId,
            final Collection<String> facilityIdsCoverage,
            final Collection<String> cityIdsCoverage,
            final Collection<String> stateIdsCoverage,
            final Collection<String> countryIdsCoverage);

    @Query(value = """
            SELECT COUNT(shp) > 0
            FROM ShipmentEntity shp
              JOIN ShipmentJourneyEntity journey ON shp.shipmentJourney.id = journey.id
              JOIN journey.packageJourneySegments segment
            WHERE shp.shipmentTrackingId = :shipmentTrackingId
              AND (
                (segment.startLocationHierarchy.externalId IN (:facilityIdsCoverage)
                OR segment.startLocationHierarchy.city.externalId IN (:cityIdsCoverage)
                OR segment.startLocationHierarchy.state.externalId IN (:stateIdsCoverage)
                OR segment.startLocationHierarchy.country.externalId IN (:countryIdsCoverage))
              OR (segment.endLocationHierarchy.externalId IN (:facilityIdsCoverage)
                OR segment.endLocationHierarchy.city.externalId IN (:cityIdsCoverage)
                OR segment.endLocationHierarchy.state.externalId IN (:stateIdsCoverage)
                OR segment.endLocationHierarchy.country.externalId IN (:countryIdsCoverage))
              )
            """)
    boolean isShipmentTrackingIdAnySegmentLocationCovered(String shipmentTrackingId, Collection<String> facilityIdsCoverage,
                                                          Collection<String> cityIdsCoverage, Collection<String> stateIdsCoverage,
                                                          Collection<String> countryIdsCoverage);

    @Query(value = """
            SELECT
              shp.id as id,
              segment.id as packageJourneySegments,
              segment.sequence as sequence,
              segment.refId as refId,
              segment.status as status,
              segment.departureTimezone as departureTimezone,
              segment.arrivalTimezone as arrivalTimezone,
              startCountry.id as startCountryId, startCountry.name as startCountryName, startCountry.externalId as startCountryExternalId, startCountry.timezone as startCountryTimezone,
              startState.id as startStateId, startState.name as startStateName, startState.externalId as startStateExternalId, startState.timezone as startStateTimezone,
              startCity.id as startCityId, startCity.name as startCityName, startCity.externalId as startCityExternalId, startCity.timezone as startCityTimezone,
              startFacility.id as startFacilityId, startFacility.name as startFacilityName, startFacility.externalId as startFacilityExternalId, startFacility.timezone as startFacilityTimezone,
              endCountry.id as endCountryId, endCountry.name as endCountryName, endCountry.externalId as endCountryExternalId, endCountry.timezone as endCountryTimezone,
              endState.id as endStateId, endState.name as endStateName, endState.externalId as endStateExternalId, endState.timezone as endStateTimezone,
              endCity.id as endCityId, endCity.name as endCityName, endCity.externalId as endCityExternalId, endCity.timezone as endCityTimezone,
              endFacility.id as endFacilityId, endFacility.name as endFacilityName, endFacility.externalId as endFacilityExternalId, endFacility.timezone as endFacilityTimezone 
            FROM ShipmentEntity shp
              LEFT JOIN shp.shipmentJourney journey
              LEFT JOIN journey.packageJourneySegments segment
              LEFT JOIN segment.startLocationHierarchy startLH
                  LEFT JOIN startLH.country startCountry
                  LEFT JOIN startLH.state startState
                  LEFT JOIN startLH.city startCity
                  LEFT JOIN startLH.facility startFacility
              LEFT JOIN segment.endLocationHierarchy endLH
                  LEFT JOIN endLH.country endCountry
                  LEFT JOIN endLH.state endState
                  LEFT JOIN endLH.city endCity
                  LEFT JOIN endLH.facility endFacility
            WHERE shp.orderId in ( select shp2.orderId from ShipmentEntity shp2 where shp2.shipmentTrackingId=:shipmentTrackingId)
              AND shp.organization.id = :organizationId
              AND segment.transportType = 'AIR'
            """)
    List<Tuple> findRelatedShipmentActiveAirSegmentsByShipmentTrackingIdAndOrganizationId(String shipmentTrackingId, String organizationId);

    @Query(value = """
            SELECT shp
            FROM ShipmentEntity shp
              LEFT JOIN shp.shipmentJourney journey
              LEFT JOIN journey.packageJourneySegments segment
            WHERE shp.organization.id = :organizationId
                AND segment.id = :segmentId
            """)
    Optional<ShipmentEntity> findBySegmentId(String segmentId, String organizationId);

    @Query(value = """
            SELECT COUNT(shp)
            FROM ShipmentEntity shp
              JOIN ShipmentJourneyEntity journey ON shp.shipmentJourney.id = journey.id
            WHERE journey.id = :journeyId
            and shp.id != :id and shp.status != :status
            """)
    int countShipmentJourneyIdAndIdNotAndStatusNot(String journeyId, String id, ShipmentStatus status);

    @Query(value = """
            SELECT shp
            FROM ShipmentEntity shp
            WHERE shp.organization.id = :organizationId
                AND shp.order.id = :orderId
            """)
    List<ShipmentEntity> findAllByOrderId(String orderId, String organizationId);

    @Query(value = """
            SELECT COUNT(shp) > 0
            FROM ShipmentEntity shp
            WHERE shp.organization.id = :organizationId
                AND shp.shipmentTrackingId = :shipmentTrackingId
            """)
    boolean isShipmentWithTrackingIdAndOrgIdExist(String shipmentTrackingId, String organizationId);

    @Query(value = """
            SELECT
              shp.id as id,
              shp.orderId as orderId,
              shp.order as order,
              shp.partnerId as partnerId,
              shp.shipmentJourneyId as shipmentJourneyId,
              shp.shipmentJourney as shipmentJourney,
              shp.shipmentTrackingId as shipmentTrackingId,
              shp.organization as organization,
              shp.shipmentPackage as shipmentPackage,
              shp.shipmentReferenceId as shipmentReferenceId,
              shp.shipmentTags as shipmentTags,
              shp.externalOrderId as externalOrderId,
              shp.internalOrderId as internalOrderId,
              shp.customerOrderId as customerOrderId,
              shp.status as status,
              shp.origin as origin,
              shp.destination as destination,
              shp.sender as sender,
              shp.consignee as consignee,
              shp.deleted as deleted,
              shp.description as description
            FROM ShipmentEntity shp
              JOIN ShipmentJourneyEntity journey ON shp.shipmentJourney.id = journey.id
            WHERE journey.id = :journeyId
              AND shp.orderId = :orderId
              AND shp.organization.id = :organizationId
            """)
    List<Tuple> findShipmentsForShipmentJourneyUpdate(String journeyId, String orderId, String organizationId);

    @Query(nativeQuery = true,
            value = SELECT_FROM_SHIPMENT +
                    "WHERE s.order_id = :orderId " +
                    "AND s.organization_id = :organizationId")
    List<ShipmentEntity> findAllShipmentsByOrderIdAndOrganizationId(String orderId, String organizationId);
}
