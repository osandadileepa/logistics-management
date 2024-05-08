package com.quincus.shipment.impl.helper;

import com.quincus.qportal.model.QPortalLocation;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.shipment.impl.service.QPortalService;
import com.quincus.web.common.multitenant.QuincusUserLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MilestoneHubLocationHandlerTest {

    @InjectMocks
    private MilestoneHubLocationHandler milestoneHubLocationHandler;
    @Mock
    private QPortalService qPortalService;
    @Mock
    private UserDetailsProvider userDetailsProvider;

    @Test
    void givenMilestoneWIthHubId_whenEnricMilestoneHubLocationIds_thenPopulateDataFromQPortalLocation() {
        Milestone milestone = new Milestone();
        milestone.setHubId("sampleHubId");

        QPortalLocation qPortalLocation = new QPortalLocation();
        qPortalLocation.setCountryId("sampleCountryId");
        qPortalLocation.setStateProvinceId("sampleStateId");
        qPortalLocation.setCityId("sampleCityId");

        when(qPortalService.getLocation(milestone.getHubId())).thenReturn(qPortalLocation);

        milestoneHubLocationHandler.enrichMilestoneHubIdWithLocationIds(milestone);

        assertThat(milestone.getHubCountryId()).isEqualTo("sampleCountryId");
        assertThat(milestone.getHubStateId()).isEqualTo("sampleStateId");
        assertThat(milestone.getHubCityId()).isEqualTo("sampleCityId");

        verify(qPortalService, times(1)).getLocation("sampleHubId");
    }

    @Test
    void givenMilestoneWIthHubCityId_whenEnrichMilestoneHubLocationDetailsByHubCityId_thenPopulateDataFromQPortalLocation() {
        Milestone milestone = new Milestone();
        milestone.setHubId("sampleHubId");
        milestone.setHubCityId("sampleHubCityId");

        QPortalLocation qPortalLocation = new QPortalLocation();
        qPortalLocation.setCountryId("sampleCountryId");
        qPortalLocation.setStateProvinceId("sampleStateId");
        qPortalLocation.setCityId("sampleCityId");
        qPortalLocation.setTimezoneTimeInGmt("test timezone");

        when(qPortalService.getLocation(milestone.getHubCityId())).thenReturn(qPortalLocation);

        milestoneHubLocationHandler.enrichMilestoneHubLocationDetailsByHubCityId(milestone);

        assertThat(milestone.getHubCountryId()).isEqualTo("sampleCountryId");
        assertThat(milestone.getHubStateId()).isEqualTo("sampleStateId");
        assertThat(milestone.getHubTimeZone()).isEqualTo("test timezone");

        verify(qPortalService, times(1)).getLocation("sampleHubCityId");
    }


    @Test
    void givenMilestoneWIthHubIdWithNoQPortalData_whenEnricMilestoneHubLocationIds_thenShouldNotThrowErrorButNoCountryStateCityHubWillBeSet() {
        Milestone milestone = new Milestone();
        milestone.setHubId("invalidHubId");

        when(qPortalService.getLocation(milestone.getHubId())).thenReturn(null);

        assertThatNoException().isThrownBy(() -> milestoneHubLocationHandler.enrichMilestoneHubIdWithLocationIds(milestone));
        assertThat(milestone.getHubCountryId()).isNull();
        assertThat(milestone.getHubCityId()).isNull();
        assertThat(milestone.getHubStateId()).isNull();

    }

    @Test
    void givenBlankMilestoneId_whenEnricMilestoneHubLocationIds_thenNoEnrichmentWOuldHappen() {
        Milestone milestone = new Milestone();
        milestone.setHubId("");

        milestoneHubLocationHandler.enrichMilestoneHubIdWithLocationIds(milestone);

        assertThat(milestone.getHubCountryId()).isNull();
        assertThat(milestone.getHubStateId()).isNull();
        assertThat(milestone.getHubCityId()).isNull();

        verify(qPortalService, never()).getLocation(anyString());
    }

    @Test
    void givenMissingCountryStateCityDetail_whenConfigureMilestoneHubWithUserHubInfo_thenShouldCheckFromQPortal() {
        Milestone milestone = new Milestone();
        QuincusUserLocation quincusUserLocation = new QuincusUserLocation();
        quincusUserLocation.locationId("sampleLocationId");
        // no country state city only facility id.

        when(userDetailsProvider.getUserCurrentLocation()).thenReturn(quincusUserLocation);

        QPortalLocation qPortalLocation = new QPortalLocation();
        qPortalLocation.setCountryId("sampleCountryId");
        qPortalLocation.setStateProvinceId("sampleStateId");
        qPortalLocation.setCityId("sampleCityId");
        when(qPortalService.getLocation(quincusUserLocation.locationId())).thenReturn(qPortalLocation);

        milestoneHubLocationHandler.configureMilestoneHubWithUserHubInfo(milestone);

        assertThat(milestone.getHubId()).isEqualTo("sampleLocationId");
        assertThat(milestone.getHubCountryId()).isEqualTo("sampleCountryId");
        assertThat(milestone.getHubStateId()).isEqualTo("sampleStateId");
        assertThat(milestone.getHubCityId()).isEqualTo("sampleCityId");

        verify(userDetailsProvider, times(1)).getUserCurrentLocation();
    }

    @Test
    void givenNoUserLocationIsProvided_whenConfigureMilestoneHubWithUserHubInfo_thenMilestoneHubIsNotSetAndNoExceptions() {
        // no user will only return new QuincusUserLocation without data
        Milestone milestone = new Milestone();

        when(userDetailsProvider.getUserCurrentLocation()).thenReturn(new QuincusUserLocation());

        milestoneHubLocationHandler.configureMilestoneHubWithUserHubInfo(milestone);

        assertThat(milestone.getHubId()).isNull();
        assertThat(milestone.getHubCountryId()).isNull();
        assertThat(milestone.getHubStateId()).isNull();
        assertThat(milestone.getHubCityId()).isNull();

        verify(userDetailsProvider, times(1)).getUserCurrentLocation();
    }
}
