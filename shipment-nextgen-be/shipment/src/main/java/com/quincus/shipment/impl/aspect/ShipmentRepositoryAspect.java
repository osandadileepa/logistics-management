package com.quincus.shipment.impl.aspect;

import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.impl.resolver.UserGroupPermissionChecker;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Aspect
@Component
@Slf4j
@AllArgsConstructor
public class ShipmentRepositoryAspect {
    private final UserGroupPermissionChecker userGroupPermissionChecker;

    @AfterReturning(
            value = "execution(* com.quincus.shipment.impl.repository.ShipmentRepository.findBy*(..))",
            returning = "shipment"
    )
    public void checkPermissionOnFindBy(Optional<ShipmentEntity> shipment) {
        shipment.ifPresent(userGroupPermissionChecker::checkUserGroupPermissions);
    }

    @AfterReturning(
            value = "execution(* com.quincus.shipment.impl.repository.ShipmentRepository.findAllByOrderIdAndOrganizationId*(..))",
            returning = "shipments"
    )
    public void checkPermissionOnFindAllByOrderIdAndOrganizationId(List<ShipmentEntity> shipments) {
        shipments.forEach(userGroupPermissionChecker::checkUserGroupPermissions);
    }

}
