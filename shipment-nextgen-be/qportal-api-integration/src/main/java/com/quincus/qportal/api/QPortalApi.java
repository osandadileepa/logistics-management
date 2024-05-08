package com.quincus.qportal.api;

import com.quincus.qportal.model.QPortalCostType;
import com.quincus.qportal.model.QPortalCurrency;
import com.quincus.qportal.model.QPortalDriver;
import com.quincus.qportal.model.QPortalLocation;
import com.quincus.qportal.model.QPortalMilestone;
import com.quincus.qportal.model.QPortalNotificationRequest;
import com.quincus.qportal.model.QPortalNotificationResponse;
import com.quincus.qportal.model.QPortalOrganization;
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

    List<QPortalPartner> listPartnersWithSearchAndPagination(@NotBlank String organizationId, @NotBlank String userId, @NotNull Integer perPage, @NotNull Integer page, String key);

    QPortalPartner getPartner(@NotBlank String organizationId, @NotBlank String partnerId);

    QPortalLocation getLocation(@NotBlank final String organizationId, @NotBlank final String locationId);

    List<QPortalLocation> getLocationsByName(@NotBlank final String organizationId, @NotBlank final String locationName);

    QPortalPartner getPartnerByName(@NotBlank final String organizationId, @NotBlank final String partnerName);

    List<QPortalUser> listUsers(@NotBlank String organizationId);

    QPortalUser getUser(@NotBlank String organizationId, @NotBlank String userId);

    QPortalUser getUserWithoutCache(@NotBlank String organizationId, @NotBlank String userId);

    QPortalUser getCurrentUserProfile(@NotBlank String token);

    List<QPortalMilestone> listMilestones(@NotBlank String organizationId);

    List<QPortalMilestone> listMilestonesWithSearchAndPagination(@NotBlank String organizationId, @NotNull Integer perPage, @NotNull Integer page, String key);

    List<QPortalCostType> listCostTypes(@NotBlank String organizationId);

    QPortalCostType getCostType(@NotBlank String organizationId, @NotBlank String costTypeId);

    List<QPortalDriver> listDrivers(@NotBlank String organizationId);

    List<QPortalDriver> listDriversByPartners(@NotBlank String organizationId, List<String> partnerIds);

    List<QPortalCurrency> listCurrencies(@NotBlank String organizationId);

    QPortalCurrency getCurrency(@NotBlank String organizationId, @NotBlank String currencyId);

    List<QPortalVehicle> listVehicles(@NotBlank String organizationId);

    List<QPortalLocation> listLocations(@NotBlank String organizationId);

    QPortalNotificationResponse sendNotification(@NotBlank String organizationId, @NotNull QPortalNotificationRequest qPortalNotificationRequest);

    QPortalOrganization getOrganizationById(@NotBlank String organizationId);

    List<QPortalVehicleType> listVehicleTypes(@NotBlank String organizationId);
}