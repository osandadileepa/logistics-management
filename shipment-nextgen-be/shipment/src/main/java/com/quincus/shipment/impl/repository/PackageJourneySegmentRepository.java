package com.quincus.shipment.impl.repository;

import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.Tuple;
import java.util.List;
import java.util.Optional;

@Repository
public interface PackageJourneySegmentRepository extends JpaRepository<PackageJourneySegmentEntity, String> {

    @Query(value = """
                    SELECT pjs.id AS id, pjs.airline AS airline, pjs.airlineCode AS airlineCode
                    FROM PackageJourneySegmentEntity pjs
                    WHERE pjs.id IN
                    (
                        SELECT MAX(ipjs.id)
                        FROM PackageJourneySegmentEntity ipjs
                        WHERE ipjs.airline IS NOT NULL AND ipjs.airline != ''
                        AND ipjs.organizationId = :organizationId
                        GROUP BY ipjs.airline
                    )
                    ORDER BY pjs.airline ASC
            """, countProjection = "DISTINCT pjs.airline")
    Page<Tuple> findAirlinesByOrganizationId(String organizationId, Pageable pageable);


    @Query(value = """
                    SELECT pjs.id AS id, pjs.flightNumber AS flightNumber
                    FROM PackageJourneySegmentEntity pjs
                    WHERE pjs.id IN
                    (
                        SELECT MAX(ipjs.id)
                        FROM PackageJourneySegmentEntity ipjs
                        WHERE ipjs.airline = :airline
                        AND ipjs.organizationId = :organizationId
                        GROUP BY ipjs.flightNumber
                    )
                    ORDER BY LENGTH(pjs.flightNumber), pjs.flightNumber ASC
            """, countProjection = "DISTINCT pjs.flightNumber")
    Page<Tuple> findFlightNumbersByAirlineAndOrganizationId(String airline, String organizationId, Pageable pageable);

    @Query(value = """
            SELECT pjs.id AS id, pjs.airline AS airline, pjs.airlineCode AS airlineCode
            FROM PackageJourneySegmentEntity pjs
            WHERE pjs.id IN (
                SELECT MAX(ipjs.id)
                FROM PackageJourneySegmentEntity ipjs
                WHERE (
                    :key IS NULL OR UPPER(ipjs.airline) LIKE CONCAT('%', UPPER(:key), '%')
                    OR
                    :key IS NULL OR UPPER(ipjs.airlineCode) LIKE CONCAT('%', UPPER(:key), '%')
                )
                AND ipjs.airline IS NOT NULL AND ipjs.airline != ''
                AND ipjs.flightNumber IS NOT NULL AND ipjs.flightNumber != ''
                AND ipjs.organizationId = :organizationId
                GROUP BY ipjs.airline
            )
            ORDER BY pjs.airline ASC
            """, countProjection = "DISTINCT pjs.airline"
    )
    Page<Tuple> findAirlinesByKeyAndOrganizationId(String key, String organizationId, Pageable pageable);


    @Query(value = """
            SELECT pjs.id AS id, pjs.airline AS airline, pjs.flightNumber AS flightNumber, pjs.airlineCode AS airlineCode
            FROM PackageJourneySegmentEntity pjs
            WHERE pjs.id IN (
                SELECT MAX(ipjs.id)
                FROM PackageJourneySegmentEntity ipjs
                WHERE (
                    :key IS NULL OR UPPER(ipjs.flightNumber) LIKE CONCAT('%', UPPER(:key), '%')
                )
                AND ipjs.airline IS NOT NULL AND ipjs.airline != ''
                AND ipjs.flightNumber IS NOT NULL AND ipjs.flightNumber != ''
                AND ipjs.organizationId = :organizationId
                GROUP BY ipjs.airline, ipjs.flightNumber
            )
            ORDER BY pjs.airline ASC
            """, countProjection = "DISTINCT pjs.flightNumber"
    )
    Page<Tuple> findFlightNumbersByKeyAndOrganizationId(String key, String organizationId, Pageable pageable);

    @Query(value = """
            SELECT
              pjs
            FROM
              PackageJourneySegmentEntity pjs
                INNER JOIN ShipmentEntity shp ON pjs.shipmentJourney.id = shp.shipmentJourney.id
            WHERE pjs.id = :id
              AND shp.id = :shipmentId
              AND pjs.organizationId = :organizationId
            """)
    Optional<PackageJourneySegmentEntity> findByIdAndOrganizationIdAndShipmentId(String id, String organizationId, String shipmentId);

    Optional<PackageJourneySegmentEntity> findByIdAndOrganizationId(String id, String organizationId);

    @Query("""
            SELECT pjs.id as id, pjs.type as type, pjs.status as status, pjs.transportType as transportType,
                   pjs.shipmentJourneyId as shipmentJourneyId, pjs.refId as refId, pjs.sequence as sequence,
                   pjs.airline as airline, pjs.flightNumber as flightNumber
            FROM PackageJourneySegmentEntity pjs
            WHERE pjs.shipmentJourneyId in (:ids)
            """)
    List<Tuple> findByShipmentJourneyIdIn(List<String> ids);


    @Query(value = """
            SELECT
              segment
            FROM
              PackageJourneySegmentEntity segment
            WHERE segment.airlineCode = :airlineCode
              AND segment.flightNumber = :flightNumber
              AND segment.departureTime LIKE CONCAT(:departureDate, '%')
              AND segment.flightOrigin = :origin
              AND segment.flightDestination = :destination
              AND segment.deleted = false
              AND segment.flight.flightId = :flightId
            """)
    List<PackageJourneySegmentEntity> findSegmentsWithFlightDetails(String airlineCode, String flightNumber, String departureDate,
                                                                    String origin, String destination, Long flightId);

    @Query(value = """
            SELECT
              segment
            FROM
              PackageJourneySegmentEntity segment
            WHERE segment.airlineCode = :airlineCode
              AND segment.flightNumber = :flightNumber
              AND segment.departureTime LIKE CONCAT(:departureDate, '%')
              AND segment.flightOrigin = :origin
              AND segment.flightDestination = :destination
              AND segment.deleted = false
              AND (segment.flightSubscriptionStatus = 'REQUEST_SENT' or segment.flightSubscriptionStatus is null)
            """)
    List<PackageJourneySegmentEntity> findSegmentsWithFlightDetailsAndSubscriptionStatusIsRequestSentOrNull(String airlineCode, String flightNumber, String departureDate,
                                                                                                            String origin, String destination);

    @Query(nativeQuery = true,
            value = """
                    SELECT DISTINCT segment.id as id,
                      segment.shipmentJourney_id as shipmentJourneyId,
                      segment.ref_id as refId,
                      segment.sequence as sequence,
                      segment.status as status,
                      segment.transport_type as transportType,
                      segment.lock_out_time as lockOutTime,
                      segment.lock_out_timezone as lockOutTimezone
                    FROM
                      package_journey_segment segment
                      INNER JOIN shipment_journey journey on journey.id = segment.shipmentJourney_id
                      INNER JOIN shipment shp on journey.id = shp.shipment_journey_id
                      AND (segment.deleted = 'false')
                    WHERE (shp.id IN (:shipmentIdList))
                    """)
    List<Tuple> findAllSegmentsFromAllShipmentIds(List<String> shipmentIdList);

    List<PackageJourneySegmentEntity> findByShipmentJourneyIdAndOrganizationId(String shipmentJourneyId, String organizationId);

    @Query(value = "SELECT * FROM package_journey_segment pjs WHERE pjs.deleted = '1'", nativeQuery = true)
    List<PackageJourneySegmentEntity> findAllByMarkedForDeletion();

    @Modifying
    @Query(value = "DELETE FROM package_journey_segment pjs WHERE pjs.deleted = '1'", nativeQuery = true)
    void deleteAllMarkedForDeletion();

}
