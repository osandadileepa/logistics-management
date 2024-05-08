package com.quincus.qportal.api;

import com.quincus.qportal.model.QPortalCurrency;
import com.quincus.qportal.model.QPortalFacility;
import com.quincus.qportal.model.QPortalOrganization;
import com.quincus.qportal.model.QPortalPartner;
import com.quincus.qportal.model.QPortalTag;
import com.quincus.qportal.model.QPortalUser;
import com.quincus.qportal.model.QPortalVehicleType;

import javax.validation.constraints.NotBlank;
import java.util.List;

public interface QPortalApi {

    QPortalUser getUserById(@NotBlank String organizationId, @NotBlank String userId);

    QPortalUser getCurrentUserProfile(@NotBlank String token);

    QPortalOrganization getOrganizationById(@NotBlank String organizationId);

    List<QPortalPartner> listPartners(@NotBlank String organizationId);

    QPortalPartner getPartnerById(@NotBlank String organizationId, @NotBlank String partnerId);

    QPortalPartner getPartnerByName(@NotBlank final String organizationId, @NotBlank final String partnerName);

    List<QPortalCurrency> listCurrencies(@NotBlank String organizationId);

    QPortalCurrency getCurrencyById(@NotBlank String organizationId, @NotBlank String currencyId);

    QPortalCurrency getCurrencyByCode(@NotBlank String organizationId, @NotBlank String currencyCode);

    List<QPortalVehicleType> listVehicleTypes(@NotBlank String organizationId);

    QPortalVehicleType getVehicleTypeById(@NotBlank String organizationId, @NotBlank String vehicleTypeId);

    QPortalVehicleType getVehicleTypeByName(@NotBlank String organizationId, @NotBlank String vehicleTypeName);

    List<QPortalFacility> listFacilities(@NotBlank String organizationId);

    QPortalFacility getFacilityById(@NotBlank String organizationId, @NotBlank String facilityId);

    QPortalFacility getFacilityByName(@NotBlank String organizationId, @NotBlank String facilityName);

    List<QPortalTag> listTags(@NotBlank String organizationId);

    QPortalTag getTagByName(@NotBlank String organizationId, @NotBlank String tagName);
}