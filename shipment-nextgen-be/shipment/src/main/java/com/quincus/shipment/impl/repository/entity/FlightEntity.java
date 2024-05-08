package com.quincus.shipment.impl.repository.entity;

import com.quincus.shipment.impl.repository.entity.component.BaseEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Entity
@Table(name = "flight")
public class FlightEntity extends BaseEntity {
    @Column(name = "flight_id", length = 20)
    private Long flightId;
    @Column(name = "carrier", length = 3)
    private String carrier;
    @Column(name = "flight_Number", length = 60)
    private String flightNumber;
    @Column(name = "departure_date", length = 60)
    private String departureDate;
    @Column(name = "origin", length = 3)
    private String origin;
    @Column(name = "destination", length = 3)
    private String destination;
    @Setter(AccessLevel.NONE)
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "flight_table_id")
    private List<FlightStatusEntity> flightStatuses;

    public void addFlightStatus(FlightStatusEntity flightStatusEntity) {
        if (flightStatusEntity == null) return;
        if (this.flightStatuses == null) {
            this.flightStatuses = new ArrayList<>();
        }
        flightStatusEntity.setFlight(this);
        this.flightStatuses.add(flightStatusEntity);
    }

    public void addAllFlightStatus(List<FlightStatusEntity> flightStatusEntityList) {
        if (CollectionUtils.isEmpty(flightStatusEntityList)) return;
        flightStatusEntityList.forEach(this::addFlightStatus);
    }

    public List<FlightStatusEntity> getFlightStatuses() {
        if (CollectionUtils.isEmpty(flightStatuses)) return Collections.emptyList();
        return flightStatuses.stream().sorted(Comparator.comparing(FlightStatusEntity::getEventDate).reversed()).toList();
    }
}
