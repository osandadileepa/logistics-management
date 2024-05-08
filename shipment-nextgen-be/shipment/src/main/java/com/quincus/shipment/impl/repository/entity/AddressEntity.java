package com.quincus.shipment.impl.repository.entity;

import com.quincus.shipment.impl.repository.entity.component.MultiTenantEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Entity
@Table(name = "address")
public class AddressEntity extends MultiTenantEntity {

    @Column(name = "external_id", length = 48)
    private String externalId;

    @Column(name = "line1", length = 200)
    private String line1;

    @Column(name = "line2", length = 200)
    private String line2;

    @Column(name = "line3", length = 200)
    private String line3;

    @Column(name = "postal_code", length = 32)
    private String postalCode;

    @Column(name = "latitude", length = 48)
    private String latitude;

    @Column(name = "longitude", length = 48)
    private String longitude;

    @Column(name = "manual_coordinates")
    private Boolean manualCoordinates;

    @Column(name = "full_address", length = 400)
    private String fullAddress;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "location_hierarchy_id", referencedColumnName = "id")
    @Fetch(FetchMode.JOIN)
    private LocationHierarchyEntity locationHierarchy;

    @Column(name = "location_hierarchy_id", insertable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private String locationHierarchyId;

    @Column(name = "company", length = 64)
    private String company;

    @Column(name = "department", length = 64)
    private String department;

}
