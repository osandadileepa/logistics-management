package com.quincus.networkmanagement.impl.service;

import com.quincus.networkmanagement.api.domain.Currency;
import com.quincus.networkmanagement.api.domain.Facility;
import com.quincus.networkmanagement.api.domain.Partner;
import com.quincus.networkmanagement.api.domain.VehicleType;
import com.quincus.networkmanagement.api.exception.QPortalSyncFailedException;
import com.quincus.networkmanagement.impl.mapper.qportal.QPortalCurrencyMapper;
import com.quincus.networkmanagement.impl.mapper.qportal.QPortalFacilityMapper;
import com.quincus.networkmanagement.impl.mapper.qportal.QPortalPartnerMapper;
import com.quincus.networkmanagement.impl.mapper.qportal.QPortalVehicleTypeMapper;
import com.quincus.qportal.api.QPortalApi;
import com.quincus.qportal.model.QPortalCurrency;
import com.quincus.qportal.model.QPortalFacility;
import com.quincus.qportal.model.QPortalModel;
import com.quincus.qportal.model.QPortalPartner;
import com.quincus.qportal.model.QPortalTag;
import com.quincus.qportal.model.QPortalVehicleType;
import com.quincus.web.common.exception.model.ApiCallException;
import com.quincus.web.common.multitenant.UserDetailsContextHolder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class QPortalService {
    private static final String ERR_SYNC_FACILITY_ID = "Failed to get facility with id `%s` from QPortal";
    private static final String ERR_SYNC_FACILITY_NAME = "Failed to get facility with name `%s` from QPortal";
    private static final String ERR_SYNC_PARTNER_ID = "Failed to get vendor/partner with id `%s` from QPortal";
    private static final String ERR_SYNC_PARTNER_NAME = "Failed to get vendor/partner with name `%s` from QPortal";
    private static final String ERR_SYNC_CURRENCY_ID = "Failed to get currency with id `%s` from QPortal";
    private static final String ERR_SYNC_CURRENCY_CODE = "Failed to get currency with code `%s` from QPortal";
    private static final String ERR_SYNC_VEHICLE_TYPE_ID = "Failed to get vehicle type with id `%s` from QPortal";
    private static final String ERR_SYNC_VEHICLE_TYPE_NAME = "Failed to get vehicle type with name `%s` from QPortal";
    private static final String ERR_SYNC_TAG_NAME = "Failed to get tag with name `%s` from QPortal";
    private static final String ERR_FACILITY_LAT_LON_MISSING = "The facility '%s' has no coordinates. " +
            "Update the facility record in QPortal and retry.";
    private static final String ERR_FACILITY_TIMEZONE_MISSING = "The facility '%s' has no timezone. " +
            "Update the facility record in QPortal and retry.";

    private final QPortalApi qPortalApi;
    private final UserDetailsContextHolder userDetailsContextHolder;
    private final QPortalFacilityMapper qPortalFacilityMapper;
    private final QPortalPartnerMapper qPortalPartnerMapper;
    private final QPortalVehicleTypeMapper qPortalVehicleTypeMapper;
    private final QPortalCurrencyMapper qPortalCurrencyMapper;

    public List<Facility> listFacilities() {
        return qPortalApi.listFacilities(userDetailsContextHolder.getCurrentOrganizationId()).stream().map(qPortalFacilityMapper::toFacility).toList();
    }

    public List<Partner> listPartners() {
        return qPortalApi.listPartners(userDetailsContextHolder.getCurrentOrganizationId()).stream().map(qPortalPartnerMapper::toPartner).toList();
    }

    public List<Currency> listCurrencies() {
        return qPortalApi.listCurrencies(userDetailsContextHolder.getCurrentOrganizationId()).stream().map(qPortalCurrencyMapper::toCurrency).toList();
    }

    public List<VehicleType> listVehicleTypes() {
        return qPortalApi.listVehicleTypes(userDetailsContextHolder.getCurrentOrganizationId()).stream().map(qPortalVehicleTypeMapper::toVehicleType).toList();
    }

    public List<String> listTags() {
        return qPortalApi.listTags(userDetailsContextHolder.getCurrentOrganizationId()).stream().map(QPortalModel::getName).toList();
    }

    private Facility getFacilityById(@NotBlank String facilityId) {
        try {
            QPortalFacility result = Optional.ofNullable(qPortalApi.getFacilityById(userDetailsContextHolder.getCurrentOrganizationId(), facilityId))
                    .orElseThrow(() -> new QPortalSyncFailedException(String.format(ERR_SYNC_FACILITY_ID, facilityId)));

            Facility facility = qPortalFacilityMapper.toFacility(result);
            validateFacility(facility);

            return facility;
        } catch (ApiCallException e) {
            throw new QPortalSyncFailedException(String.format(ERR_SYNC_FACILITY_ID, facilityId));
        }
    }

    private Facility getFacilityByName(@NotBlank String facilityName) {
        try {
            QPortalFacility result = Optional.ofNullable(qPortalApi.getFacilityByName(userDetailsContextHolder.getCurrentOrganizationId(), facilityName))
                    .orElseThrow(() -> new QPortalSyncFailedException(String.format(ERR_SYNC_FACILITY_NAME, facilityName)));

            Facility facility = qPortalFacilityMapper.toFacility(result);
            validateFacility(facility);

            return facility;
        } catch (ApiCallException e) {
            throw new QPortalSyncFailedException(String.format(ERR_SYNC_FACILITY_NAME, facilityName));
        }
    }

    /**
     * Validates if the facility has the required information
     * - Lat/lon must be present
     * - Timezone must be present
     */
    private void validateFacility(Facility facility) {
        if (facility.getLat() == null || facility.getLon() == null) {
            throw new QPortalSyncFailedException(String.format(ERR_FACILITY_LAT_LON_MISSING, facility.getName()));
        }
        if(StringUtils.isBlank(facility.getTimezone())) {
            throw new QPortalSyncFailedException(String.format(ERR_FACILITY_TIMEZONE_MISSING, facility.getName()));
        }
    }

    private Partner getPartnerById(@NotBlank String partnerId) {
        try {
            QPortalPartner result = Optional.ofNullable(qPortalApi.getPartnerById(userDetailsContextHolder.getCurrentOrganizationId(), partnerId))
                    .orElseThrow(() -> new QPortalSyncFailedException(String.format(ERR_SYNC_PARTNER_ID, partnerId)));

            return qPortalPartnerMapper.toPartner(result);
        } catch (ApiCallException e) {
            throw new QPortalSyncFailedException(String.format(ERR_SYNC_PARTNER_ID, partnerId));
        }
    }

    private Partner getPartnerByName(@NotBlank String partnerName) {
        try {
            QPortalPartner result = Optional.ofNullable(qPortalApi.getPartnerByName(userDetailsContextHolder.getCurrentOrganizationId(), partnerName))
                    .orElseThrow(() -> new QPortalSyncFailedException(String.format(ERR_SYNC_PARTNER_NAME, partnerName)));

            return qPortalPartnerMapper.toPartner(result);
        } catch (ApiCallException e) {
            throw new QPortalSyncFailedException(String.format(ERR_SYNC_PARTNER_NAME, partnerName));
        }
    }

    private VehicleType getVehicleTypeById(@NotBlank String vehicleTypeId) {
        try {
            QPortalVehicleType result = Optional.ofNullable(qPortalApi.getVehicleTypeById(userDetailsContextHolder.getCurrentOrganizationId(), vehicleTypeId))
                    .orElseThrow(() -> new QPortalSyncFailedException(String.format(ERR_SYNC_VEHICLE_TYPE_ID, vehicleTypeId)));

            return qPortalVehicleTypeMapper.toVehicleType(result);
        } catch (ApiCallException e) {
            throw new QPortalSyncFailedException(String.format(ERR_SYNC_VEHICLE_TYPE_ID, vehicleTypeId));
        }
    }

    private VehicleType getVehicleTypeByName(@NotBlank String vehicleTypeName) {
        try {
            QPortalVehicleType result = Optional.ofNullable(qPortalApi.getVehicleTypeByName(userDetailsContextHolder.getCurrentOrganizationId(), vehicleTypeName))
                    .orElseThrow(() -> new QPortalSyncFailedException(String.format(ERR_SYNC_VEHICLE_TYPE_NAME, vehicleTypeName)));

            return qPortalVehicleTypeMapper.toVehicleType(result);
        } catch (ApiCallException e) {
            throw new QPortalSyncFailedException(String.format(ERR_SYNC_VEHICLE_TYPE_NAME, vehicleTypeName));
        }
    }

    private Currency getCurrencyById(@NotBlank String currencyId) {
        try {
            QPortalCurrency result = Optional.ofNullable(qPortalApi.getCurrencyById(userDetailsContextHolder.getCurrentOrganizationId(), currencyId))
                    .orElseThrow(() -> new QPortalSyncFailedException(String.format(ERR_SYNC_CURRENCY_ID, currencyId)));

            return qPortalCurrencyMapper.toCurrency(result);
        } catch (ApiCallException e) {
            throw new QPortalSyncFailedException(String.format(ERR_SYNC_CURRENCY_ID, currencyId));
        }
    }

    private Currency getCurrencyByCode(@NotBlank String currencyCode) {
        try {
            QPortalCurrency result = Optional.ofNullable(qPortalApi.getCurrencyByCode(userDetailsContextHolder.getCurrentOrganizationId(), currencyCode))
                    .orElseThrow(() -> new QPortalSyncFailedException(String.format(ERR_SYNC_CURRENCY_CODE, currencyCode)));

            return qPortalCurrencyMapper.toCurrency(result);
        } catch (ApiCallException e) {
            throw new QPortalSyncFailedException(String.format(ERR_SYNC_CURRENCY_CODE, currencyCode));
        }
    }

    private void validateTag(@NotBlank String tag) {
        try {
            QPortalTag result = qPortalApi.getTagByName(userDetailsContextHolder.getCurrentOrganizationId(), tag);
            if (result == null) {
                throw new QPortalSyncFailedException(String.format(ERR_SYNC_TAG_NAME, tag));
            }
        } catch (ApiCallException e) {
            throw new QPortalSyncFailedException(String.format(ERR_SYNC_TAG_NAME, tag));
        }
    }

    public Facility syncFacility(Facility facility) {
        if (facility == null || StringUtils.isAllBlank(facility.getId(), facility.getName()))
            return null;

        if (StringUtils.isNotBlank(facility.getId())) {
            return getFacilityById(facility.getId());
        }
        return getFacilityByName(facility.getName());
    }

    public Partner syncPartner(Partner partner) {
        if (partner == null || StringUtils.isAllBlank(partner.getId(), partner.getName()))
            return null;

        if (StringUtils.isNotBlank(partner.getId())) {
            return getPartnerById(partner.getId());
        }
        return getPartnerByName(partner.getName());
    }

    public Currency syncCurrency(Currency currency) {
        if (currency == null || StringUtils.isAllBlank(currency.getId(), currency.getCode()))
            return null;

        if (StringUtils.isNotBlank(currency.getId())) {
            return getCurrencyById(currency.getId());
        }
        return getCurrencyByCode(currency.getCode());
    }

    public VehicleType syncVehicleType(VehicleType vehicleType) {
        if (vehicleType == null || StringUtils.isAllBlank(vehicleType.getId(), vehicleType.getName()))
            return null;

        if (StringUtils.isNotBlank(vehicleType.getId())) {
            return getVehicleTypeById(vehicleType.getId());
        }
        return getVehicleTypeByName(vehicleType.getName());
    }

    public void validateTags(List<String> tags) {
        if (tags != null) {
            tags.forEach(this::validateTag);
        }
    }
}
