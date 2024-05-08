package com.quincus.finance.costing.ratecard.impl.web;

import com.quincus.finance.costing.ratecard.api.model.RateCalculationInput;
import com.quincus.finance.costing.ratecard.api.model.RateCalculationOutput;
import com.quincus.finance.costing.ratecard.impl.RateCalculationController;
import com.quincus.finance.costing.ratecard.impl.service.RateCalculationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class RateCalculationControllerImpl implements RateCalculationController {

    private final RateCalculationService rateCalculationService;

    @Override
    public ResponseEntity<RateCalculationOutput> calculate(RateCalculationInput request) {
        return ResponseEntity.ok(rateCalculationService.calculate(request));
    }

}
