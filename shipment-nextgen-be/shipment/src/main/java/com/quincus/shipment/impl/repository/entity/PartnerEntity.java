package com.quincus.shipment.impl.repository.entity;

import com.quincus.shipment.impl.repository.entity.component.MultiTenantEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Entity
@Table(name = "partner")
public class PartnerEntity extends MultiTenantEntity {

    @Column(name = "external_id", length = 48)
    private String externalId;

    @EqualsAndHashCode.Include
    @ToString.Include
    @Column(name = "name", length = 128)
    private String name;

    @Column(name = "code", length = 128)
    private String code;

    @Column(name = "type", length = 128)
    private String partnerType;

    @Column(name = "contact_name", length = 128)
    private String contactName;

    @Column(name = "contact_number", length = 50)
    private String contactNumber;

    @Column(name = "contact_code", length = 50)
    private String contactCode;

    @Column(name = "email", length = 128)
    private String email;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", referencedColumnName = "id")
    private AddressEntity address;
}