package com.quincus.finance.costing.ratecard.impl.service;

import com.quincus.finance.costing.common.exception.CostingApiException;
import com.quincus.finance.costing.ratecard.api.RateCardApi;
import com.quincus.finance.costing.ratecard.api.model.RateCard;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class RateCardService {

    public static final String ERR_NOT_FOUND = "Rate Card with Id %s not found";

    private final RateCardApi rateCardApi;

    @Transactional
    public RateCard create(RateCard rateCard) {
        return rateCardApi.create(rateCard);
    }

    public RateCard update(RateCard rateCard) {
        return rateCardApi.update(rateCard)
                .orElseThrow(() -> new CostingApiException(
                        String.format(ERR_NOT_FOUND, rateCard.getId()), HttpStatus.NOT_FOUND
                ));
    }

    public RateCard get(String id) {
        return rateCardApi.find(id)
                .orElseThrow(() -> new CostingApiException(
                        String.format(ERR_NOT_FOUND, id), HttpStatus.NOT_FOUND
                ));
    }

    @Transactional
    public void delete(String id) {
        rateCardApi.delete(id);
    }
}
