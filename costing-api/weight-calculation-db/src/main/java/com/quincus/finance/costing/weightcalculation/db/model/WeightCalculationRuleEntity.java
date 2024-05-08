package com.quincus.finance.costing.weightcalculation.db.model;

import com.quincus.db.model.BaseEntity;
import com.quincus.db.model.OrganizationEntity;
import com.quincus.db.model.PartnerEntity;
import com.quincus.finance.costing.weightcalculation.api.model.ChargeableWeightRule;
import com.quincus.finance.costing.weightcalculation.api.model.VolumeWeightRule;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Where;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
@Entity
@Table(name = "weight_calculation_rule")
@Where(clause = "deleted = false")
public class WeightCalculationRuleEntity extends BaseEntity {

    @Id
    @Column(name = "id", nullable = false, columnDefinition = "CHAR(36)")
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @EqualsAndHashCode.Include
    @ToString.Include
    private String id;

    @Column(name = "deleted")
    private boolean deleted = Boolean.FALSE;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "active")
    private boolean active = Boolean.TRUE;

    @Column(name = "chargeable_weight_rule")
    private ChargeableWeightRule chargeableWeightRule;

    @Column(name = "chargeable_weight_min")
    private BigDecimal chargeableWeightMin;

    @Column(name = "chargeable_weight_max")
    private BigDecimal chargeableWeightMax;

    @Column(name = "volume_weight_rule")
    private VolumeWeightRule volumeWeightRule;

    @Column(name = "standard_volume_weight_rule_divisor")
    private BigDecimal standardVolumeWeightRuleDivisor;

    @Column(name = "volume_weight_min")
    private BigDecimal volumeWeightMin;

    @Column(name = "volume_weight_max")
    private BigDecimal volumeWeightMax;

    @Column(name = "actual_weight_min")
    private BigDecimal actualWeightMin;

    @Column(name = "actual_weight_max")
    private BigDecimal actualWeightMax;

    @Column(name = "actual_weight_rounding")
    private boolean actualWeightRounding = Boolean.FALSE;

    @Column(name = "volume_weight_rounding")
    private boolean volumeWeightRounding = Boolean.FALSE;

    @Column(name = "chargeable_weight_rounding")
    private boolean chargeableWeightRounding = Boolean.FALSE;

    @Column(name = "rounding_place")
    private BigDecimal roundingPlace;

    @Column(name = "rounding_threshold")
    private BigDecimal roundingThreshold;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "weight_calculation_rule_partner",
            joinColumns = {@JoinColumn(name = "weight_calculation_rule_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "partner_id", referencedColumnName = "id")})
    @ToString.Exclude
    private Set<PartnerEntity> partners = new HashSet<>();

    @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "organization_id", nullable = false, referencedColumnName = "id")
    @ToString.Exclude
    private OrganizationEntity organization;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "special_volume_weight_rule_id", referencedColumnName = "id")
    @ToString.Exclude
    private SpecialVolumeWeightRuleEntity specialVolumeWeightRule;
}
