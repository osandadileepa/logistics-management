package com.quincus.finance.costing.ratecard.api;

import com.quincus.finance.costing.ratecard.api.model.RateCard;

import java.util.Optional;

public interface RateCardApi {

    RateCard create(RateCard rateCard);

    Optional<RateCard> find(String id);

    Optional<RateCard> update(RateCard rateCard);

    void delete(String id);

}
