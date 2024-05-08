package com.quincus.qportal.api;

import com.quincus.qportal.model.QPortalCostType;
import com.quincus.qportal.model.QPortalCurrency;
import com.quincus.qportal.model.QPortalDriver;
import com.quincus.qportal.model.QPortalLocation;
import com.quincus.qportal.model.QPortalMilestone;
import com.quincus.qportal.model.QPortalNotificationResponse;
import com.quincus.qportal.model.QPortalOrganization;
import com.quincus.qportal.model.QPortalPackageType;
import com.quincus.qportal.model.QPortalPartner;
import com.quincus.qportal.model.QPortalUser;
import com.quincus.qportal.model.QPortalUserRequest;
import com.quincus.qportal.model.QPortalVehicle;
import com.quincus.qportal.model.QPortalVehicleType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QPortalApiImplTest {

    @InjectMocks
    QPortalApiImpl qPortalApi;
    @Mock
    QPortalRestClient qPortalRestClient;

    @Test
    void listPackageTypes_ShouldReturnResult() {
        when(qPortalRestClient.listPackageTypes(anyString())).thenReturn(List.of(new QPortalPackageType()));
        assertThat(qPortalApi.listPackageTypes(anyString())).isNotNull();
    }

    @Test
    void listCostTypes_ShouldReturnResult() {
        when(qPortalRestClient.listCostTypes(anyString())).thenReturn(List.of(new QPortalCostType()));
        assertThat(qPortalApi.listCostTypes(anyString())).isNotNull();
    }

    @Test
    void listCurrencies_ShouldReturnResult() {
        when(qPortalRestClient.listCurrencies(anyString())).thenReturn(List.of(new QPortalCurrency()));
        assertThat(qPortalApi.listCurrencies(anyString())).isNotNull();
    }

    @Test
    void listDriversShouldReturnResult() {
        when(qPortalRestClient.listDrivers(anyString())).thenReturn(List.of(new QPortalDriver()));
        assertThat(qPortalApi.listDrivers(anyString())).isNotNull();
    }

    @Test
    void listDriversByPartnersShouldReturnResult() {
        when(qPortalRestClient.listDriversByPartners(anyString(), any(QPortalUserRequest.class))).thenReturn(List.of(new QPortalDriver()));
        assertThat(qPortalApi.listDriversByPartners(anyString(), any())).isNotNull();
    }

    @Test
    void getCostType_ShouldReturnResult() {
        when(qPortalRestClient.getCostType(anyString(), anyString())).thenReturn(new QPortalCostType());
        assertThat(qPortalApi.getCostType(anyString(), anyString())).isNotNull();
    }

    @Test
    void getCurrency_ShouldReturnResult() {
        when(qPortalRestClient.getCurrency(anyString(), anyString())).thenReturn(new QPortalCurrency());
        assertThat(qPortalApi.getCurrency(anyString(), anyString())).isNotNull();
    }

    @Test
    void getUserById_ShouldReturnResult() {
        when(qPortalRestClient.getUserById(anyString(), anyString())).thenReturn(new QPortalUser());
        assertThat(qPortalApi.getUser(anyString(), anyString())).isNotNull();
    }

    @Test
    void listPartnersV2_shouldCallQPortalRestClientV2() {
        String organizationId = "org-123";
        Integer page = 1;
        Integer perPage = 10;
        String key = "test";
        String userId = UUID.randomUUID().toString();
        when(qPortalRestClient.listPartnersWithSearchAndPagination(organizationId, userId, perPage, page, key)).thenReturn(new ArrayList<>());
        assertThat(qPortalApi.listPartnersWithSearchAndPagination(organizationId, userId, perPage, page, key)).isNotNull();
        verify(qPortalRestClient, times(1)).listPartnersWithSearchAndPagination(organizationId, userId, perPage, page, key);
    }

    @Test
    void listPartners_ShouldReturnResult() {
        when(qPortalRestClient.listPartners(anyString())).thenReturn(List.of(new QPortalPartner()));
        assertThat(qPortalApi.listPartners(anyString())).isNotNull();
    }

    @Test
    void getPartner_ShouldReturnResult() {
        when(qPortalRestClient.getPartnerById(anyString(), anyString())).thenReturn(new QPortalPartner());
        assertThat(qPortalApi.getPartner(anyString(), anyString())).isNotNull();
    }

    @Test
    void getPartnerByName_ShouldReturnResult() {
        when(qPortalRestClient.getPartnerByName(anyString(), anyString())).thenReturn(new QPortalPartner());
        assertThat(qPortalApi.getPartnerByName(anyString(), anyString())).isNotNull();
    }

    @Test
    void getLocation_ShouldReturnResult() {
        when(qPortalRestClient.getLocation(anyString(), anyString())).thenReturn(new QPortalLocation());
        assertThat(qPortalApi.getLocation(anyString(), anyString())).isNotNull();
    }

    @Test
    void listUsers_ShouldReturnResult() {
        when(qPortalRestClient.listUsers(anyString())).thenReturn(List.of(new QPortalUser()));
        assertThat(qPortalApi.listUsers(anyString())).isNotNull();
    }

    @Test
    void getUser_ShouldReturnResult() {
        when(qPortalRestClient.getUserById(anyString(), anyString())).thenReturn(new QPortalUser());
        assertThat(qPortalApi.getUser(anyString(), anyString())).isNotNull();
    }

    @Test
    void getCurrentUserProfile_ShouldReturnResult() {
        when(qPortalRestClient.getCurrentUserProfile(anyString())).thenReturn(new QPortalUser());
        assertThat(qPortalApi.getCurrentUserProfile(anyString())).isNotNull();
    }

    @Test
    void getLocationsByName_ShouldReturnResult() {
        when(qPortalRestClient.getLocationsByName(anyString(), anyString())).thenReturn(List.of(new QPortalLocation()));
        assertThat(qPortalApi.getLocationsByName(anyString(), anyString())).isNotNull();
    }

    @Test
    void listMilestones_ShouldReturnResult() {
        when(qPortalRestClient.listMilestones(anyString())).thenReturn(List.of(new QPortalMilestone()));
        assertThat(qPortalApi.listMilestones(anyString())).isNotNull();
    }

    @Test
    void listMilestonesWithPageParam_ShouldReturnResult() {
        String organizationId = "82027602-402a-41f7-82dc-80cfbb011b4e";
        int perPage = 10;
        int page = 1;
        when(qPortalRestClient.listMilestonesWithSearchAndPagination(organizationId, perPage, page, null)).thenReturn(List.of(new QPortalMilestone()));
        assertThat(qPortalApi.listMilestonesWithSearchAndPagination(organizationId, perPage, page, null)).isNotNull();
    }


    @Test
    void listVehicles_ShouldReturnResult() {
        when(qPortalRestClient.listVehicles(anyString())).thenReturn(List.of(new QPortalVehicle()));
        assertThat(qPortalApi.listVehicles(anyString())).isNotNull();
    }

    @Test
    void listLocations_ShouldReturnResult() {
        when(qPortalRestClient.listLocations(anyString())).thenReturn(List.of(new QPortalLocation()));
        assertThat(qPortalApi.listLocations(anyString())).isNotNull();
    }

    @Test
    void getOrganizationById_ShouldReturnResult() {
        when(qPortalRestClient.getOrganizationById(anyString())).thenReturn(new QPortalOrganization());
        assertThat(qPortalApi.getOrganizationById(anyString())).isNotNull();
    }

    @Test
    void getVehicleTypes_ShouldReturnResult() {
        when(qPortalRestClient.listVehicleTypes(anyString())).thenReturn(List.of(new QPortalVehicleType()));
        assertThat(qPortalApi.listVehicleTypes(anyString())).isNotNull();
    }

    @Test
    void sendNotification_ShouldReturnResult() {
        when(qPortalRestClient.sendNotification(anyString(), any())).thenReturn(new QPortalNotificationResponse());
        assertThat(qPortalApi.sendNotification(anyString(), any())).isNotNull();
    }

    @Test
    void getUserWithoutCache_ShouldReturnResult() {
        when(qPortalRestClient.getUserById(anyString(), anyString())).thenReturn(new QPortalUser());
        assertThat(qPortalApi.getUserWithoutCache(anyString(), anyString())).isNotNull();
    }

}
