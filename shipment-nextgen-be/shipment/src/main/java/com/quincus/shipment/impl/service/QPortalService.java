package com.quincus.shipment.impl.service;

import com.quincus.qportal.api.QPortalApi;
import com.quincus.qportal.model.QPortalLocation;
import com.quincus.qportal.model.QPortalNotificationRequest;
import com.quincus.qportal.model.QPortalNotificationResponse;
import com.quincus.shipment.api.domain.CostType;
import com.quincus.shipment.api.domain.Currency;
import com.quincus.shipment.api.domain.Driver;
import com.quincus.shipment.api.domain.MilestoneLookup;
import com.quincus.shipment.api.domain.PackageType;
import com.quincus.shipment.api.domain.Partner;
import com.quincus.shipment.api.domain.User;
import com.quincus.shipment.api.domain.VehicleType;
import com.quincus.shipment.api.dto.MilestoneResponse;
import com.quincus.shipment.impl.mapper.qportal.QPortalCostTypeMapper;
import com.quincus.shipment.impl.mapper.qportal.QPortalCurrencyMapper;
import com.quincus.shipment.impl.mapper.qportal.QPortalDriverMapper;
import com.quincus.shipment.impl.mapper.qportal.QPortalMilestoneMapper;
import com.quincus.shipment.impl.mapper.qportal.QPortalPackageTypeMapper;
import com.quincus.shipment.impl.mapper.qportal.QPortalPartnerMapper;
import com.quincus.shipment.impl.mapper.qportal.QPortalUserMapper;
import com.quincus.shipment.impl.mapper.qportal.QPortalVehicleTypeMapper;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class QPortalService {
    private final QPortalApi qPortalApi;
    private final QPortalMilestoneMapper qPortalMilestoneMapper;
    private final QPortalPartnerMapper qPortalPartnerMapper;
    private final QPortalUserMapper qPortalUserMapper;
    private final QPortalPackageTypeMapper qPortalPackageTypeMapper;
    private final QPortalCostTypeMapper qPortalCostTypeMapper;
    private final QPortalDriverMapper qPortalDriverMapper;
    private final QPortalCurrencyMapper qPortalCurrencyMapper;
    private final QPortalVehicleTypeMapper qPortalVehicleTypeMapper;
    private final UserDetailsProvider userDetailsProvider;

    public List<PackageType> listPackageTypes() {
        return qPortalApi.listPackageTypes(userDetailsProvider.getCurrentOrganizationId()).stream().map(qPortalPackageTypeMapper::toPackageType).toList();
    }

    public List<Partner> listPartners() {
        return qPortalApi.listPartners(userDetailsProvider.getCurrentOrganizationId()).stream().map(qPortalPartnerMapper::toPartner).toList();
    }

    public List<Partner> listPartnersWithSearchAndPagination(int perPage, int page, String key) {
        return qPortalApi.listPartnersWithSearchAndPagination(userDetailsProvider.getCurrentOrganizationId(), userDetailsProvider.getCurrentUserId(), perPage, page, key).stream().map(qPortalPartnerMapper::toPartner).toList();
    }


    public Partner getPartnerById(@NotBlank String partnerId) {
        return qPortalPartnerMapper.toPartner(qPortalApi.getPartner(userDetailsProvider.getCurrentOrganizationId(), partnerId));
    }

    public List<User> listUsers() {
        return qPortalApi.listUsers(userDetailsProvider.getCurrentOrganizationId()).stream().map(qPortalUserMapper::toUser).toList();
    }

    public User getUser(@NotBlank String userId) {
        return qPortalUserMapper.toUser(qPortalApi.getUser(userDetailsProvider.getCurrentOrganizationId(), userId));
    }

    public User getUserWithoutCache(@NotBlank String organizationId, @NotBlank String userId) {
        return qPortalUserMapper.toUser(qPortalApi.getUserWithoutCache(organizationId, userId));
    }

    public QPortalLocation getLocation(@NotBlank String locationId) {
        return qPortalApi.getLocation(userDetailsProvider.getCurrentOrganizationId(), locationId);
    }

    public List<MilestoneLookup> listMilestones() {
        return qPortalApi.listMilestones(userDetailsProvider.getCurrentOrganizationId()).stream().map(qPortalMilestoneMapper::toMilestoneLookup).toList();
    }

    public List<MilestoneLookup> listMilestonesWithSearchAndPagination(int perPage, int page, String key) {
        return qPortalApi.listMilestonesWithSearchAndPagination(userDetailsProvider.getCurrentOrganizationId(), perPage, page, key).stream().map(qPortalMilestoneMapper::toMilestoneLookup).toList();
    }

    public List<MilestoneLookup> listMilestonesByOrganizationId(@NotBlank String organizationId) {
        return qPortalApi.listMilestones(organizationId).stream().map(qPortalMilestoneMapper::toMilestoneLookup).toList();
    }

    public List<MilestoneResponse> listMilestoneCodes() {
        return qPortalApi.listMilestones(userDetailsProvider.getCurrentOrganizationId()).stream().map(qPortalMilestoneMapper::toMilestoneCodeResponse).toList();
    }

    public List<CostType> listCostTypes() {
        return qPortalApi.listCostTypes(userDetailsProvider.getCurrentOrganizationId()).stream().map(qPortalCostTypeMapper::toCostType).toList();
    }

    public CostType getCostType(@NotBlank String costTypeId) {
        return qPortalCostTypeMapper.toCostType(qPortalApi.getCostType(userDetailsProvider.getCurrentOrganizationId(), costTypeId));
    }

    public List<Driver> listDrivers() {
        return qPortalApi.listDrivers(userDetailsProvider.getCurrentOrganizationId()).stream().map(qPortalDriverMapper::toDriver).toList();

        // todo code reverted while waiting for QPortal changes, use new implementation below
//        List<String> partnerIds = userDetailsProvider.getCurrentUserPartners().stream().map(QuincusUserPartner::getPartnerId).sorted().toList();
//        return qPortalApi.listDriversByPartners(
//                userDetailsProvider.getCurrentOrganizationId(),
//                partnerIds
//        ).stream().map(qPortalDriverMapper::toDriver).toList();
    }

    public List<Currency> listCurrencies() {
        return qPortalApi.listCurrencies(userDetailsProvider.getCurrentOrganizationId()).stream().map(qPortalCurrencyMapper::toCurrency).toList();
    }

    public QPortalNotificationResponse sendNotification(@NotBlank String organizationId, @NotNull QPortalNotificationRequest qPortalNotification) {
        return qPortalApi.sendNotification(organizationId, qPortalNotification);
    }

    public Currency getCurrency(@NotBlank String currencyId) {
        return qPortalCurrencyMapper.toCurrency(qPortalApi.getCurrency(userDetailsProvider.getCurrentOrganizationId(), currencyId));
    }

    public List<VehicleType> listVehicleTypes() {
        return qPortalApi.listVehicleTypes(userDetailsProvider.getCurrentOrganizationId()).stream().map(qPortalVehicleTypeMapper::toVehicleType).toList();
    }
}
