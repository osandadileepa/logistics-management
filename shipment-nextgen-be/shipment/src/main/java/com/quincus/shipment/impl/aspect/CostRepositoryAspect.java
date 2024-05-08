package com.quincus.shipment.impl.aspect;

import com.quincus.shipment.impl.repository.entity.CostEntity;
import com.quincus.shipment.impl.resolver.LocationPermissionChecker;
import com.quincus.shipment.impl.resolver.UserGroupPermissionChecker;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Aspect
@Slf4j
@AllArgsConstructor
@Component
public class CostRepositoryAspect {

    private final LocationPermissionChecker locationPermissionChecker;
    private final UserGroupPermissionChecker userGroupPermissionChecker;

    @AfterReturning(
            value = "execution(* com.quincus.shipment.impl.repository.CostRepository.findById(..))",
            returning = "cost"
    )
    public void checkPermissionOnFindBy(Optional<CostEntity> cost) {
        cost.ifPresent(locationPermissionChecker::checkLocationPermissions);
        cost.ifPresent(userGroupPermissionChecker::checkUserGroupPermissions);
    }

}
