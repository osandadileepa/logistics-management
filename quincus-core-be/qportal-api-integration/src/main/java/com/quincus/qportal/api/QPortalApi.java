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

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

public interface QPortalApi {

    List<QPortalPackageType> listPackageTypes(@NotBlank String organizationId);

    List<QPortalPartner> listPartners(@NotBlank String organizationId);

    QPortalPartner getPartner(@NotBlank String organizationId, @NotBlank String partnerId);

    QPortalLocation getLocation(@NotBlank final String organizationId, @NotBlank final String locationId);

    QPortalLocation getLocationByName(@NotBlank final String organizationId, @NotBlank final String locationName);

    QPortalPartner getPartnerByName(@NotBlank final String organizationId, @NotBlank final String partnerName);

    List<QPortalUser> listUsers(@NotBlank String organizationId);

    QPortalUser getUser(@NotBlank String organizationId, @NotBlank String userId);

    QPortalUser getCurrentUserProfile(@NotBlank String token);

    List<QPortalMilestone> listMilestones(@NotBlank String organizationId);

    List<QPortalCostType> listCostTypes(@NotBlank String organizationId);

    QPortalCostType getCostType(@NotBlank String organizationId, @NotBlank String costTypeId);

    List<QPortalDriver> listDrivers(@NotBlank String organizationId);

    List<QPortalCurrency> listCurrencies(@NotBlank String organizationId);

    QPortalCurrency getCurrency(@NotBlank String organizationId, @NotBlank String currencyId);

    List<QPortalVehicle> listVehicles(@NotBlank String organizationId);

    List<QPortalVehicleType> listVehicleTypes(@NotBlank String organizationId);

    QPortalVehicleType getVehicleType(@NotBlank String organizationId, @NotBlank String vehicleTypeId);

    List<QPortalLocation> listLocations(@NotBlank String organizationId);

    QPortalNotificationResponse sendNotificationQcomm(@NotBlank String organizationId, @NotNull QPortalNotification qPortalNotification);

    List<QPortalFacility> listFacilities(@NotBlank String organizationId);

    QPortalFacility getFacility(@NotBlank String organizationId, @NotBlank String facilityId);

    QPortalFacility getFacilityByName(@NotBlank String organizationId, @NotBlank String facilityName);
}
