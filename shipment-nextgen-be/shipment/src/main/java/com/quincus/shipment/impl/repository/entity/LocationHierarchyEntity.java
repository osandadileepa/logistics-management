package com.quincus.shipment.impl.repository.entity;

import com.quincus.shipment.impl.repository.entity.component.MultiTenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Objects;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Entity
@Table(name = "location_hierarchy")
public class LocationHierarchyEntity extends MultiTenantEntity {

    @Column(name = "country_id", nullable = false, length = 48)
    private String countryId;

    @ManyToOne
    @JoinColumn(name = "country_id", referencedColumnName = "id", insertable = false, updatable = false)
    private LocationEntity country;

    @Column(name = "country_code", length = 64)
    private String countryCode;

    @Column(name = "state_id", nullable = false, length = 48)
    private String stateId;

    @ManyToOne
    @JoinColumn(name = "state_id", referencedColumnName = "id", insertable = false, updatable = false)
    private LocationEntity state;

    @Column(name = "state_code", length = 64)
    private String stateCode;

    @Column(name = "city_id", nullable = false, length = 48)
    private String cityId;

    @ManyToOne
    @JoinColumn(name = "city_id", referencedColumnName = "id", insertable = false, updatable = false)
    private LocationEntity city;

    @Column(name = "city_code", length = 64)
    private String cityCode;

    @Column(name = "active")
    private boolean active;

    @Column(name = "ext_id", length = 48)
    private String externalId;

    @Column(name = "facility_id", length = 48)
    private String facilityId;

    @OneToOne
    @JoinColumn(name = "facility_id", referencedColumnName = "id", insertable = false, updatable = false)
    private LocationEntity facility;

    @Column(name = "facility_code", length = 64)
    private String facilityCode;

    @Column(name = "facility_name") // not set as setting 255 in length complains redundant default parameter
    private String facilityName;

    @Column(name = "facility_location_code", length = 64)
    private String facilityLocationCode;

    public void setCountry(@NonNull LocationEntity country) {
        this.country = country;
        this.countryId = country.getId();
    }

    public void setState(@NonNull LocationEntity state) {
        this.state = state;
        this.stateId = state.getId();

    }

    public void setCity(@NonNull LocationEntity city) {
        this.city = city;
        this.cityId = city.getId();
    }

    public void setFacility(LocationEntity facility) {
        this.facility = facility;
        if (Objects.nonNull(facility)) {
            this.facilityId = facility.getId();
        }
    }

}
