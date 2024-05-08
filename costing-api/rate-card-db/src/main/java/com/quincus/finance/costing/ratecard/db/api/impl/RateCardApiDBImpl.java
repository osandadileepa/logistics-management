package com.quincus.finance.costing.ratecard.db.api.impl;

import com.quincus.finance.costing.ratecard.api.RateCardApi;
import com.quincus.finance.costing.ratecard.api.model.RateCard;
import com.quincus.finance.costing.ratecard.db.mapper.RateCardMapper;
import com.quincus.finance.costing.ratecard.db.model.RateCardEntity;
import com.quincus.finance.costing.ratecard.db.repository.RateCardRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@AllArgsConstructor
@Service
public class RateCardApiDBImpl implements RateCardApi {

    private final RateCardRepository rateCardRepository;

    private final RateCardMapper mapper;


    @Override
    public RateCard create(RateCard rateCard) {
        return mapper.mapEntityToDomain(
                rateCardRepository.save(
                        mapper.mapDomainToEntity(rateCard)
                )
        );
    }

    @Override
    public Optional<RateCard> find(String id) {
        return findById(id).map(mapper::mapEntityToDomain);
    }

    @Override
    public Optional<RateCard> update(RateCard rateCard) {
        Optional<RateCardEntity> entity = findById(rateCard.getId());
        return entity.map(it -> mapper.mapEntityToDomain(
                rateCardRepository.save(
                        mapper.update(rateCard, it)
                )
        ));
    }

    @Override
    public void delete(String id) {
        Optional<RateCardEntity> entity = findById(id);
        if (entity.isPresent()) {
            entity.get().setDeleted(true);
            rateCardRepository.save(entity.get());
        }
    }

    private Optional<RateCardEntity> findById(String id) {
        return rateCardRepository.findById(id);
    }

}
