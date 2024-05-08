package com.quincus.qportal.api;

import com.quincus.qportal.model.QPortalCurrency;
import com.quincus.qportal.model.QPortalFacility;
import com.quincus.qportal.model.QPortalOrganization;
import com.quincus.qportal.model.QPortalPartner;
import com.quincus.qportal.model.QPortalTag;
import com.quincus.qportal.model.QPortalUser;
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
    @Cacheable(value = "userById", key = "#organizationId + '-' + #userId")
    public QPortalUser getUserById(@NotBlank String organizationId, @NotBlank String userId) {
        return qPortalRestClient.getUserById(organizationId, userId);
    }

    @Override
    @Cacheable(value = "currentUserProfile", key = "#token")
    public QPortalUser getCurrentUserProfile(String token) {
        return qPortalRestClient.getCurrentUserProfile(token);
    }

    @Override
    @Cacheable(value = "organizationById", key = "#organizationId")
    public QPortalOrganization getOrganizationById(String organizationId) {
        return qPortalRestClient.getOrganizationById(organizationId);
    }

    @Override
    public List<QPortalPartner> listPartners(@NotBlank String organizationId) {
        return qPortalRestClient.listPartners(organizationId);
    }

    @Override
    @Cacheable(value = "partnerById", key = "#organizationId + '-' + #partnerId")
    public QPortalPartner getPartnerById(@NotBlank String organizationId, @NotBlank String partnerId) {
        return qPortalRestClient.getPartnerById(organizationId, partnerId);
    }

    @Override
    @Cacheable(value = "partnerByName", key = "#organizationId + '-' + #partnerName")
    public QPortalPartner getPartnerByName(@NotBlank final String organizationId, @NotBlank final String partnerName) {
        return qPortalRestClient.getPartnerByName(organizationId, partnerName);
    }

    @Override
    public List<QPortalCurrency> listCurrencies(@NotBlank String organizationId) {
        return qPortalRestClient.listCurrencies(organizationId);
    }

    @Override
    @Cacheable(value = "currencyById", key = "#organizationId + '-' + #currencyId")
    public QPortalCurrency getCurrencyById(@NotBlank String organizationId, @NotBlank String currencyId) {
        return qPortalRestClient.getCurrencyById(organizationId, currencyId);
    }

    @Override
    @Cacheable(value = "currencyByCode", key = "#organizationId + '-' + #currencyCode")
    public QPortalCurrency getCurrencyByCode(String organizationId, String currencyCode) {
        return qPortalRestClient.getCurrencyByCode(organizationId, currencyCode);
    }

    @Override
    public List<QPortalVehicleType> listVehicleTypes(String organizationId) {
        return qPortalRestClient.listVehicleTypes(organizationId);
    }

    @Override
    @Cacheable(value = "vehicleTypeById", key = "#organizationId + '-' + #vehicleTypeId")
    public QPortalVehicleType getVehicleTypeById(String organizationId, String vehicleTypeId) {
        return qPortalRestClient.getVehicleTypeById(organizationId, vehicleTypeId);
    }

    @Override
    @Cacheable(value = "vehicleTypeByName", key = "#organizationId + '-' + #vehicleTypeName")
    public QPortalVehicleType getVehicleTypeByName(String organizationId, String vehicleTypeName) {
        return qPortalRestClient.getVehicleTypeByName(organizationId, vehicleTypeName);
    }

    @Override
    public List<QPortalFacility> listFacilities(@NotBlank String organizationId) {
        return qPortalRestClient.listFacilities(organizationId);
    }

    @Override
    @Cacheable(value = "facilityById", key = "#organizationId + '-' + #facilityId")
    public QPortalFacility getFacilityById(String organizationId, String facilityId) {
        return qPortalRestClient.getFacilityById(organizationId, facilityId);
    }

    @Override
    @Cacheable(value = "facilityByName", key = "#organizationId + '-' + #facilityName")
    public QPortalFacility getFacilityByName(String organizationId, String facilityName) {
        return qPortalRestClient.getFacilityByName(organizationId, facilityName);
    }

    @Override
    public List<QPortalTag> listTags(String organizationId) {
        return qPortalRestClient.listTags(organizationId);
    }

    @Override
    @Cacheable(value = "tagByName", key = "#organizationId + '-' + #tagName")
    public QPortalTag getTagByName(String organizationId, String tagName) {
        return qPortalRestClient.getTagByName(organizationId, tagName);
    }
}
