package com.quincus.shipment.impl.aspect;

import com.quincus.shipment.impl.repository.entity.CostEntity;
import com.quincus.shipment.impl.resolver.LocationPermissionChecker;
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
class CostRepositoryAspectTest {

    @Mock
    private LocationPermissionChecker locationPermissionChecker;

    @Mock
    private UserGroupPermissionChecker userGroupPermissionChecker;

    @InjectMocks
    private CostRepositoryAspect costRepositoryAspect;

    @Test
    void testCheckPermissionOnFindBy() {
        CostEntity costEntity = new CostEntity();
        Optional<CostEntity> optionalCostEntity = Optional.of(costEntity);

        costRepositoryAspect.checkPermissionOnFindBy(optionalCostEntity);

        verify(locationPermissionChecker, times(1)).checkLocationPermissions(costEntity);
        verify(userGroupPermissionChecker, times(1)).checkUserGroupPermissions(costEntity);
    }
}
