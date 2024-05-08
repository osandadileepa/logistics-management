package com.quincus.qportal.api;

import com.quincus.qportal.model.QPortalCostType;
import com.quincus.qportal.model.QPortalCurrency;
import com.quincus.qportal.model.QPortalDriver;
import com.quincus.qportal.model.QPortalFacility;
import com.quincus.qportal.model.QPortalLocation;
import com.quincus.qportal.model.QPortalMilestone;
import com.quincus.qportal.model.QPortalNotification;
import com.quincus.qportal.model.QPortalNotificationResponse;
import com.quincus.qportal.model.QPortalPackageType;
import com.quincus.qportal.model.QPortalPartner;
import com.quincus.qportal.model.QPortalUser;
import com.quincus.qportal.model.QPortalVehicle;
import com.quincus.qportal.model.QPortalVehicleType;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;
import java.util.List;

@AllArgsConstructor
@Service
public class QPortalApiImpl implements QPortalApi {

    private final QPortalRestClient qPortalRestClient;

    @Override
    public List<QPortalPackageType> listPackageTypes(@NotBlank String organizationId) {
        return qPortalRestClient.listPackageTypes(organizationId);
    }

    @Override
    public List<QPortalPartner> listPartners(@NotBlank String organizationId) {
        return qPortalRestClient.listPartners(organizationId);
    }

    @Cacheable(value = "partner", key = "#organizationId + '-' + #partnerId")
    @Override
    public QPortalPartner getPartner(@NotBlank String organizationId, @NotBlank String partnerId) {
        return qPortalRestClient.getPartnerById(organizationId, partnerId);
    }

    @Cacheable(value = "partnerByName", key = "#organizationId + '-' + #partnerName")
    @Override
    public QPortalPartner getPartnerByName(@NotBlank final String organizationId, @NotBlank final String partnerName) {
        return qPortalRestClient.getPartnerByName(organizationId, partnerName);
    }

    @Cacheable(value = "locationById", key = "#organizationId + '-' + #locationId")
    @Override
    public QPortalLocation getLocation(@NotBlank final String organizationId, @NotBlank final String locationId) {
        return qPortalRestClient.getLocation(organizationId, locationId);
    }

    @Cacheable(value = "locationByName", key = "#organizationId + '-' + #locationName")
    @Override
    public QPortalLocation getLocationByName(@NotBlank final String organizationId, @NotBlank final String locationName) {
        return qPortalRestClient.getLocationByName(organizationId, locationName);
    }

    @Override
    public List<QPortalUser> listUsers(@NotBlank String organizationId) {
        return qPortalRestClient.listUsers(organizationId);
    }

    @Override
    public QPortalUser getUser(@NotBlank String organizationId, @NotBlank String userId) {
        return qPortalRestClient.getUserById(organizationId, userId);
    }

    @Override
    @Cacheable(value = "currentUserProfile", key = "#token")
    public QPortalUser getCurrentUserProfile(String token) {
        return qPortalRestClient.getCurrentUserProfile(token);
    }

    @Override
    @Cacheable(value = "milestones", key = "#organizationId + '-milestones'")
    public List<QPortalMilestone> listMilestones(@NotBlank String organizationId) {
        return qPortalRestClient.listMilestones(organizationId);
    }

    @Override
    public List<QPortalCostType> listCostTypes(@NotBlank String organizationId) {
        return qPortalRestClient.listCostTypes(organizationId);
    }

    @Override
    public QPortalCostType getCostType(@NotBlank String organizationId, @NotBlank String costTypeId) {
        return qPortalRestClient.getCostType(organizationId, costTypeId);
    }

    @Override
    @Cacheable(value = "driver", key = "#organizationId + '-drivers'")
    public List<QPortalDriver> listDrivers(@NotBlank String organizationId) {
        return qPortalRestClient.listDrivers(organizationId);
    }

    @Override
    public List<QPortalCurrency> listCurrencies(@NotBlank String organizationId) {
        return qPortalRestClient.listCurrencies(organizationId);
    }

    @Override
    public QPortalCurrency getCurrency(@NotBlank String organizationId, @NotBlank String currencyId) {
        return qPortalRestClient.getCurrency(organizationId, currencyId);
    }

    @Override
    @Cacheable(value = "vehicle", key = "#organizationId + '-vehicles'")
    public List<QPortalVehicle> listVehicles(@NotBlank String organizationId) {
        return qPortalRestClient.listVehicles(organizationId);
    }

    @Override
    @Cacheable(value = "vehicle", key = "#organizationId + '-vehicle-types'")
    public List<QPortalVehicleType> listVehicleTypes(String organizationId) {
        return qPortalRestClient.listVehicleTypes(organizationId);
    }

    @Override
    @Cacheable(value = "vehicle", key = "#organizationId + '-vehicle-type-' + #vehicleTypeId")
    public QPortalVehicleType getVehicleType(String organizationId, String vehicleTypeId) {
        return qPortalRestClient.getVehicleType(organizationId, vehicleTypeId);
    }

    @Override
    @Cacheable(value = "location", key = "#organizationId + '-locations'")
    public List<QPortalLocation> listLocations(@NotBlank String organizationId) {
        return qPortalRestClient.listLocations(organizationId);
    }

    @Override
    public QPortalNotificationResponse sendNotificationQcomm(String organizationId, QPortalNotification qPortalNotification) {
        return qPortalRestClient.sendNotification(organizationId, qPortalNotification);
    }


    @Override
    public List<QPortalFacility> listFacilities(@NotBlank String organizationId) {
        return qPortalRestClient.listFacilities(organizationId);
    }

    @Override
    public QPortalFacility getFacility(String organizationId, String facilityId) {
        return qPortalRestClient.getFacility(organizationId, facilityId);
    }

    @Override
    public QPortalFacility getFacilityByName(String organizationId, String facilityName) {
        return qPortalRestClient.getFacilityByName(organizationId, facilityName);
    }
}
