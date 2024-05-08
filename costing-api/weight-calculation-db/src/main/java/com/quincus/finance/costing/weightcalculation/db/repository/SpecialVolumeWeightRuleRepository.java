package com.quincus.finance.costing.weightcalculation.db.repository;

import com.quincus.finance.costing.weightcalculation.db.model.SpecialVolumeWeightRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecialVolumeWeightRuleRepository extends JpaRepository<SpecialVolumeWeightRuleEntity, String> { }
