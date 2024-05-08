package com.quincus.shipment.impl.aspect;

import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.impl.resolver.UserGroupPermissionChecker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ShipmentRepositoryAspectTest {

    @Mock
    private UserGroupPermissionChecker userGroupPermissionChecker;

    @InjectMocks
    private ShipmentRepositoryAspect shipmentRepositoryAspect;

    @Test
    void testCheckPermissionOnFindBy() {
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        Optional<ShipmentEntity> optionalShipmentEntity = Optional.of(shipmentEntity);

        shipmentRepositoryAspect.checkPermissionOnFindBy(optionalShipmentEntity);

        verify(userGroupPermissionChecker, times(1)).checkUserGroupPermissions(shipmentEntity);
    }

}
