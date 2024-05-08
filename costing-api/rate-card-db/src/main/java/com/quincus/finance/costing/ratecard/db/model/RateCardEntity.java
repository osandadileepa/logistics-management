package com.quincus.finance.costing.ratecard.db.model;

import com.quincus.db.model.BaseEntity;
import com.quincus.finance.costing.ratecard.api.model.RateCardCalculationType;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
@Entity
@Table(name = "rate_card")
@Where(clause = "deleted = false")
public class RateCardEntity extends BaseEntity {

    @Id
    @Column(name = "id", nullable = false, columnDefinition = "CHAR(36)")
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @EqualsAndHashCode.Include
    @ToString.Include
    private String id;

    @Column(name = "deleted")
    private boolean deleted = Boolean.FALSE;

    @Column(name = "calculation_type")
    private RateCardCalculationType calculationType;

    @Column(name = "rate_value")
    private BigDecimal rateValue;

    @Column(name = "min")
    private BigDecimal min;

    @Column(name = "max")
    private BigDecimal max;
}
