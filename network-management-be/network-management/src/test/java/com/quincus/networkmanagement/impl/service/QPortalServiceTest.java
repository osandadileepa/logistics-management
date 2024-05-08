package com.quincus.networkmanagement.impl.service;

import com.quincus.networkmanagement.api.domain.Facility;
import com.quincus.networkmanagement.api.exception.QPortalSyncFailedException;
import com.quincus.networkmanagement.impl.mapper.qportal.QPortalFacilityMapper;
import com.quincus.qportal.api.QPortalApi;
import com.quincus.qportal.model.QPortalFacility;
import com.quincus.web.common.multitenant.UserDetailsContextHolder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ContextConfiguration;

import java.util.UUID;

import static com.quincus.networkmanagement.impl.data.NetworkManagementTestData.dummyFacility;
import static com.quincus.networkmanagement.impl.data.NetworkManagementTestData.dummyQPortalFacility;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {QPortalService.class})
class QPortalServiceTest {

    private final String orgId = UUID.randomUUID().toString();
    @Mock
    private QPortalApi qPortalApi;
    @Spy
    private QPortalFacilityMapper qPortalFacilityMapper = Mappers.getMapper(QPortalFacilityMapper.class);
    @Mock
    private UserDetailsContextHolder userDetailsContextHolder;
    @InjectMocks
    private QPortalService qPortalService;

    @Test
    void syncValidFacilityShouldNotThrowException() {
        Facility facility = dummyFacility();
        QPortalFacility qPortalFacility = dummyQPortalFacility();

        when(userDetailsContextHolder.getCurrentOrganizationId()).thenReturn(orgId);
        when(qPortalApi.getFacilityById(orgId, facility.getId())).thenReturn(qPortalFacility);

        assertThatCode(() -> qPortalService.syncFacility(facility)).doesNotThrowAnyException();
    }

    @Test
    void syncFacilityWithoutTimezoneShouldThrowException() {
        Facility facility = dummyFacility();
        QPortalFacility qPortalFacility = dummyQPortalFacility();
        qPortalFacility.setTimezoneTimeInGmt(null);

        when(userDetailsContextHolder.getCurrentOrganizationId()).thenReturn(orgId);
        when(qPortalApi.getFacilityById(orgId, facility.getId())).thenReturn(qPortalFacility);

        assertThatThrownBy(() -> qPortalService.syncFacility(facility)).isInstanceOf(QPortalSyncFailedException.class)
                .hasMessageContaining("no timezone");
    }

    @Test
    void syncFacilityWithoutCoordinatesShouldThrowException() {
        Facility facility = dummyFacility();
        QPortalFacility qPortalFacility = dummyQPortalFacility();
        qPortalFacility.setLat(null);
        qPortalFacility.setLon(null);

        when(userDetailsContextHolder.getCurrentOrganizationId()).thenReturn(orgId);
        when(qPortalApi.getFacilityById(orgId, facility.getId())).thenReturn(qPortalFacility);

        assertThatThrownBy(() -> qPortalService.syncFacility(facility)).isInstanceOf(QPortalSyncFailedException.class)
                .hasMessageContaining("no coordinates");
    }
}
