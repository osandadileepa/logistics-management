package com.quincus.shipment.impl.repository.entity;

import com.quincus.shipment.api.constant.FlightEventName;
import com.quincus.shipment.api.constant.FlightEventType;
import com.quincus.shipment.api.constant.FlightStatusCode;
import com.quincus.shipment.impl.repository.entity.component.BaseEntity;
import com.quincus.shipment.impl.repository.entity.component.FlightDetailsEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Entity
@Table(name = "flight_status")
public class FlightStatusEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_table_id", nullable = false)
    private FlightEntity flight;
    @Column(name = "flight_id")
    private Long flightId;
    @Column(name = "status", length = 2)
    @Enumerated(EnumType.STRING)
    private FlightStatusCode status;
    @Column(name = "airline_code", length = 3)
    private String airlineCode;
    @Column(name = "airline_name", length = 60)
    private String airlineName;
    @Column(name = "operating_airline_code", length = 3)
    private String operatingAirlineCode;
    @Column(name = "longitude", length = 60)
    private String longitude;
    @Column(name = "latitude", length = 60)
    private String latitude;
    @Column(name = "speed_mph", length = 60)
    private String speedMph;
    @Column(name = "altitude_ft", length = 60)
    private String altitudeFt;
    @Column(name = "event_date")
    private String eventDate;
    @Column(name = "event_type", length = 60)
    @Enumerated(EnumType.STRING)
    private FlightEventType eventType;
    @Column(name = "event_name", length = 60)
    @Enumerated(EnumType.STRING)
    private FlightEventName eventName;
    @Embedded
    @AttributeOverride(name = "airportCode", column = @Column(name = "departure_airport_code"))
    @AttributeOverride(name = "airportName", column = @Column(name = "departure_airport_name"))
    @AttributeOverride(name = "scheduledTime", column = @Column(name = "departure_scheduled_time"))
    @AttributeOverride(name = "estimatedTime", column = @Column(name = "departure_estimated_time"))
    @AttributeOverride(name = "actualTime", column = @Column(name = "departure_actual_time"))
    @AttributeOverride(name = "timezone", column = @Column(name = "departure_timezone"))
    private FlightDetailsEntity departure;
    @Embedded
    @AttributeOverride(name = "airportCode", column = @Column(name = "arrival_airport_code"))
    @AttributeOverride(name = "airportName", column = @Column(name = "arrival_airport_name"))
    @AttributeOverride(name = "scheduledTime", column = @Column(name = "arrival_scheduled_time"))
    @AttributeOverride(name = "estimatedTime", column = @Column(name = "arrival_estimated_time"))
    @AttributeOverride(name = "actualTime", column = @Column(name = "arrival_actual_time"))
    @AttributeOverride(name = "timezone", column = @Column(name = "arrival_timezone"))
    private FlightDetailsEntity arrival;

}
