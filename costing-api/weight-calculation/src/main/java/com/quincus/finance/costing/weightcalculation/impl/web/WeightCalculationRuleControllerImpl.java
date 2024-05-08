package com.quincus.finance.costing.weightcalculation.impl.web;

import com.quincus.finance.costing.common.web.model.filter.CostingApiFilterResult;
import com.quincus.finance.costing.weightcalculation.api.filter.WeightCalculationRuleFilter;
import com.quincus.finance.costing.weightcalculation.api.model.WeightCalculationRule;
import com.quincus.finance.costing.weightcalculation.impl.WeightCalculationRuleController;
import com.quincus.finance.costing.weightcalculation.impl.service.WeightCalculationRuleService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Validated
@AllArgsConstructor
@RequestMapping("/weight-calculation-rules")
public class WeightCalculationRuleControllerImpl implements WeightCalculationRuleController {

    private final WeightCalculationRuleService weightCalculationRuleService;

    @Override
    public ResponseEntity<WeightCalculationRule> create(WeightCalculationRule data, MultipartFile file) {
        return ResponseEntity.ok(weightCalculationRuleService.create(data, file));
    }

    @Override
    public ResponseEntity<WeightCalculationRule> update(String id, WeightCalculationRule data, MultipartFile file) {
        data.setId(id);
        return ResponseEntity.ok(weightCalculationRuleService.update(data, file));
    }

    @Override
    public ResponseEntity<WeightCalculationRule> get(String id) {
        return ResponseEntity.ok(weightCalculationRuleService.get(id));
    }

    @Override
    public ResponseEntity<CostingApiFilterResult<WeightCalculationRule>> search(WeightCalculationRuleFilter filter,
                                                                                Pageable pageable
    ) {
        return ResponseEntity.ok(new CostingApiFilterResult<>(weightCalculationRuleService.search(filter, pageable)));
    }

    @Override
    public void delete(String id) {
        weightCalculationRuleService.delete(id);
    }

    @Override
    public ResponseEntity<WeightCalculationRule> getDefaultRuleByOrganizationId(String organizationId) {
        return ResponseEntity.ok(weightCalculationRuleService.getRuleByOrganizationId(organizationId));
    }

}
