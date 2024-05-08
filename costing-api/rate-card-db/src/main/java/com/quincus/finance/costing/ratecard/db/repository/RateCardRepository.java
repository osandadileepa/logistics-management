package com.quincus.finance.costing.ratecard.db.repository;

import com.quincus.finance.costing.ratecard.db.model.RateCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RateCardRepository extends JpaRepository<RateCardEntity, String> {
}
