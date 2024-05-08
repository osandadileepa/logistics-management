package com.quincus.shipment.impl.web;

import com.quincus.qportal.model.QPortalLocation;
import com.quincus.shipment.QPortalController;
import com.quincus.shipment.api.domain.CostType;
import com.quincus.shipment.api.domain.Currency;
import com.quincus.shipment.api.domain.Driver;
import com.quincus.shipment.api.domain.MilestoneLookup;
import com.quincus.shipment.api.domain.PackageType;
import com.quincus.shipment.api.domain.Partner;
import com.quincus.shipment.api.domain.User;
import com.quincus.shipment.api.domain.VehicleType;
import com.quincus.shipment.api.dto.MilestoneResponse;
import com.quincus.shipment.impl.service.QPortalService;
import com.quincus.web.common.model.Response;
import com.quincus.web.common.utility.annotation.LogExecutionTime;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/qportal")
@AllArgsConstructor
@Validated
public class QPortalControllerImpl implements QPortalController {

    private final QPortalService qPortalService;

    @Override
    @PreAuthorize("hasAnyAuthority('SHIPMENTS_EDIT', 'SHIPMENTS_EXPORT', 'SHIPMENTS_VIEW', 'DIMS_AND_WEIGHT_VIEW', 'DIMS_AND_WEIGHT_EDIT', 'SHIPMENT_STATUS_EDIT', 'SHIPMENT_STATUS_VIEW')")
    @LogExecutionTime
    public Response<List<PackageType>> listPackageTypes() {
        return new Response<>(qPortalService.listPackageTypes());
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SHIPMENTS_EDIT', 'SHIPMENTS_EXPORT', 'SHIPMENTS_VIEW', 'SHIPMENT_STATUS_EDIT', 'SHIPMENT_STATUS_VIEW')")
    @LogExecutionTime
    public Response<List<Partner>> listPartners() {
        return new Response<>(qPortalService.listPartners());
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SHIPMENTS_EDIT', 'SHIPMENTS_EXPORT', 'SHIPMENTS_VIEW', 'SHIPMENT_STATUS_EDIT', 'SHIPMENT_STATUS_VIEW')")
    @LogExecutionTime
    public Response<List<Partner>> listPartners(final int perPage, final int page, final String key) {
        return new Response<>(qPortalService.listPartnersWithSearchAndPagination(perPage, page, key));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SHIPMENTS_EDIT', 'SHIPMENTS_EXPORT', 'SHIPMENTS_VIEW', 'SHIPMENT_STATUS_EDIT', 'SHIPMENT_STATUS_VIEW')")
    @LogExecutionTime
    public Response<Partner> getPartner(final String partnerId) {
        return new Response<>(qPortalService.getPartnerById(partnerId));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SHIPMENTS_EDIT', 'SHIPMENTS_EXPORT', 'SHIPMENTS_VIEW', 'SHIPMENT_STATUS_EDIT', 'SHIPMENT_STATUS_VIEW')")
    @LogExecutionTime
    public Response<List<User>> listUsers() {
        return new Response<>(qPortalService.listUsers());
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SHIPMENTS_EDIT', 'SHIPMENTS_EXPORT', 'SHIPMENTS_VIEW', 'SHIPMENT_STATUS_EDIT', 'SHIPMENT_STATUS_VIEW')")
    @LogExecutionTime
    public Response<User> getUser(final String userId) {
        return new Response<>(qPortalService.getUser(userId));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SHIPMENTS_EDIT', 'SHIPMENTS_EXPORT', 'SHIPMENTS_VIEW', 'SHIPMENT_STATUS_EDIT', 'SHIPMENT_STATUS_VIEW')")
    @LogExecutionTime
    public Response<QPortalLocation> getLocation(final String locationId) {
        return new Response<>(qPortalService.getLocation(locationId));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SHIPMENTS_EDIT', 'SHIPMENTS_EXPORT', 'SHIPMENTS_VIEW', 'SHIPMENT_STATUS_EDIT', 'SHIPMENT_STATUS_VIEW')")
    @LogExecutionTime
    public Response<List<MilestoneLookup>> listMilestones() {
        return new Response<>(qPortalService.listMilestones());
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SHIPMENTS_EDIT', 'SHIPMENTS_EXPORT', 'SHIPMENTS_VIEW', 'SHIPMENT_STATUS_EDIT', 'SHIPMENT_STATUS_VIEW')")
    @LogExecutionTime
    public Response<List<MilestoneLookup>> listMilestones(final int perPage, final int page, final String key) {
        return new Response<>(qPortalService.listMilestonesWithSearchAndPagination(perPage, page, key));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SHIPMENTS_EDIT', 'SHIPMENTS_EXPORT', 'SHIPMENTS_VIEW', 'SHIPMENT_STATUS_EDIT', 'SHIPMENT_STATUS_VIEW')")
    public Response<List<MilestoneResponse>> listMilestoneCodes() {
        return new Response<>(qPortalService.listMilestoneCodes());
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SHIPMENTS_EDIT', 'SHIPMENTS_EXPORT', 'SHIPMENTS_VIEW', 'SHIPMENT_STATUS_EDIT', 'SHIPMENT_STATUS_VIEW', 'COST_CREATE', 'COST_EDIT', 'COST_VIEW')")
    @LogExecutionTime
    public Response<List<CostType>> listCostTypes() {
        return new Response<>(qPortalService.listCostTypes());
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SHIPMENTS_EDIT', 'SHIPMENTS_EXPORT', 'SHIPMENTS_VIEW', 'SHIPMENT_STATUS_EDIT', 'SHIPMENT_STATUS_VIEW', 'COST_CREATE', 'COST_EDIT', 'COST_VIEW')")
    @LogExecutionTime
    public Response<List<Driver>> listDrivers() {
        return new Response<>(qPortalService.listDrivers());
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SHIPMENTS_EDIT', 'SHIPMENTS_EXPORT', 'SHIPMENTS_VIEW', 'SHIPMENT_STATUS_EDIT', 'SHIPMENT_STATUS_VIEW', 'COST_CREATE', 'COST_EDIT', 'COST_VIEW')")
    @LogExecutionTime
    public Response<List<Currency>> listCurrencies() {
        return new Response<>(qPortalService.listCurrencies());
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SHIPMENTS_EDIT', 'SHIPMENTS_EXPORT', 'SHIPMENTS_VIEW', 'SHIPMENT_STATUS_EDIT', 'SHIPMENT_STATUS_VIEW')")
    @LogExecutionTime
    public Response<List<VehicleType>> listVehicleTypes() {
        return new Response<>(qPortalService.listVehicleTypes());
    }
}