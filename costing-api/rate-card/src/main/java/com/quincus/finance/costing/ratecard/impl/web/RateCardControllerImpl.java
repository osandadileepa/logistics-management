package com.quincus.finance.costing.ratecard.impl.web;

import com.quincus.finance.costing.ratecard.api.model.RateCard;
import com.quincus.finance.costing.ratecard.impl.RateCardController;
import com.quincus.finance.costing.ratecard.impl.service.RateCardService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class RateCardControllerImpl implements RateCardController {

    private final RateCardService rateCardService;

    @Override
    public ResponseEntity<RateCard> create(RateCard request) {
        return ResponseEntity.ok(rateCardService.create(request));
    }

    @Override
    public ResponseEntity<RateCard> update(String id, RateCard request) {
        request.setId(id);
        return ResponseEntity.ok(rateCardService.update(request));
    }

    @Override
    public ResponseEntity<RateCard> get(String id) {
        return ResponseEntity.ok(rateCardService.get(id));
    }

    @Override
    public void delete(String id) {
        rateCardService.delete(id);
    }

}
