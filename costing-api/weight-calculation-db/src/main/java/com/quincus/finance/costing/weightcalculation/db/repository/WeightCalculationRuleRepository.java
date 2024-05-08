package com.quincus.finance.costing.weightcalculation.db.repository;

import com.quincus.finance.costing.weightcalculation.db.model.WeightCalculationRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface WeightCalculationRuleRepository extends JpaRepository<WeightCalculationRuleEntity, String>, JpaSpecificationExecutor<WeightCalculationRuleEntity> {

    @Query(value = "SELECT * " +
            "  FROM weight_calculation_rule wcr " +
            "  WHERE wcr.organization_id = :organizationId AND active = :isActive AND deleted = :isDeleted" +
            "  AND EXISTS (SELECT NULL " +
            "  FROM weight_calculation_rule_partner wcrp " +
            "  WHERE wcr.id = wcrp.weight_calculation_rule_id" +
            "  AND wcrp.partner_id IN (:partnerIds))",
            nativeQuery = true)
    List<WeightCalculationRuleEntity> findActiveByOrganizationIdAndPartnerIds(String organizationId, Set<String> partnerIds, boolean isActive, boolean isDeleted);

    @Query(value = "SELECT * " +
            "  FROM weight_calculation_rule wcr " +
            "  WHERE wcr.organization_id = :organizationId AND active = :isActive AND deleted = :isDeleted" +
            "  AND NOT EXISTS (SELECT NULL " +
            "  FROM weight_calculation_rule_partner wcrp " +
            "  WHERE wcr.id = wcrp.weight_calculation_rule_id )",
            nativeQuery = true)
    List<WeightCalculationRuleEntity> findActiveByOrganizationId(String organizationId, boolean isActive, boolean isDeleted);

}
