package com.quincus.finance.costing.weightcalculation.impl.web;

import com.quincus.finance.costing.weightcalculation.api.model.WeightCalculationInput;
import com.quincus.finance.costing.weightcalculation.api.model.WeightCalculationOutput;
import com.quincus.finance.costing.weightcalculation.impl.WeightCalculationController;
import com.quincus.finance.costing.weightcalculation.impl.service.WeightCalculationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class WeightCalculationControllerImpl implements WeightCalculationController {

    private final WeightCalculationService weightCalculationService;

    @Override
    public ResponseEntity<WeightCalculationOutput> calculate(WeightCalculationInput request) {
        return ResponseEntity.ok(weightCalculationService.calculate(request));
    }

}
