package com.quincus.shipment.impl.repository;

import com.quincus.shipment.impl.repository.entity.CostEntity;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CostRepository extends JpaRepository<CostEntity, String>, JpaSpecificationExecutor<CostEntity> {
    //Need to override this method as the Aspect trigger this method.
    @Override
    @NonNull
    Optional<CostEntity> findById(@NonNull String id);
}
