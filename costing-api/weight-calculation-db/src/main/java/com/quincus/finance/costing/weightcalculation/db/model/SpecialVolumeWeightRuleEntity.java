package com.quincus.finance.costing.weightcalculation.db.model;

import com.quincus.finance.costing.weightcalculation.api.model.Conversion;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.util.ArrayList;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "special_volume_weight_rule")
public class SpecialVolumeWeightRuleEntity {

    @Id
    @Column(name = "id", nullable = false, columnDefinition = "CHAR(36)")
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @EqualsAndHashCode.Include
    @ToString.Include
    private String id;

    @Column(name = "file_uploaded")
    private String fileUploaded;

    @Column(name = "custom_formula")
    private String customFormula;

    @Column(name = "conversions")
    
    @Lob
    ArrayList<Conversion> conversions;

}
