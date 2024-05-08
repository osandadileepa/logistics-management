package com.quincus.finance.costing.weightcalculation.impl.web;

import com.quincus.finance.costing.weightcalculation.api.model.CheckRoundingLogicInput;
import com.quincus.finance.costing.weightcalculation.api.model.CheckRoundingLogicOutput;
import com.quincus.finance.costing.weightcalculation.impl.RoundingLogicController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static com.quincus.finance.costing.common.util.RoundingLogicUtil.applyRoundingLogic;

@RestController
public class RoundingLogicControllerImpl implements RoundingLogicController {

    @Override
    public ResponseEntity<CheckRoundingLogicOutput> calculate(CheckRoundingLogicInput request) {
        return ResponseEntity.ok(new CheckRoundingLogicOutput(applyRoundingLogic(
                request.getValue(),
                request.getRoundingLogic()
        )));
    }

}
