package com.quincus.shipment;

import com.quincus.ext.annotation.UUID;
import com.quincus.qportal.model.QPortalLocation;
import com.quincus.shipment.api.domain.CostType;
import com.quincus.shipment.api.domain.Currency;
import com.quincus.shipment.api.domain.Driver;
import com.quincus.shipment.api.domain.MilestoneLookup;
import com.quincus.shipment.api.domain.PackageType;
import com.quincus.shipment.api.domain.Partner;
import com.quincus.shipment.api.domain.User;
import com.quincus.shipment.api.domain.VehicleType;
import com.quincus.shipment.api.dto.MilestoneResponse;
import com.quincus.web.common.model.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.List;

@Validated
@RequestMapping("/qportal")
@Tag(name = "qportal", description = "This endpoint acts as an gateway for QPortal API.")
public interface QPortalController {

    @GetMapping("/package_types")
    @Operation(summary = "QPortal Package Types API", description = "Return a list of package types from QPortal.", tags = "qportal")
    Response<List<PackageType>> listPackageTypes();

    @GetMapping("/partners")
    @Operation(summary = "QPortal List Partners API", description = "Return a list of partners from QPortal.", tags = "qportal")
    Response<List<Partner>> listPartners();

    @GetMapping("/v2/partners")
    @Operation(summary = "QPortal List Partners API", description = "Return a list of partners from QPortal.", tags = "qportal")
    Response<List<Partner>> listPartners(@RequestParam(value = "per_page", defaultValue = "10000") @Min(1) @Max(10000) final int perPage,
                                         @RequestParam(value = "page", defaultValue = "1") @Min(1) @Max(Integer.MAX_VALUE) final int page,
                                         @RequestParam(required = false, value = "key") @Size(min = 1, max = 256) final String key);

    @GetMapping("/partners/{partnerId}")
    @Operation(summary = "QPortal Partner API", description = "Find an existing partner by partnerId from QPortal.", tags = "qportal")
    Response<Partner> getPartner(@PathVariable @UUID final String partnerId);

    @GetMapping("/users")
    @Operation(summary = "QPortal List Users API", description = "Return a list of users from QPortal.", tags = "qportal")
    Response<List<User>> listUsers();

    @GetMapping("/users/{userId}")
    @Operation(summary = "QPortal User API", description = "Find an existing user by userId from QPortal.", tags = "qportal")
    Response<User> getUser(@PathVariable @UUID final String userId);

    @GetMapping("/locations/{locationId}")
    @Operation(summary = "QPortal Location API", description = "Find an existing location by locationId from QPortal.", tags = "qportal")
    Response<QPortalLocation> getLocation(@PathVariable @UUID final String locationId);

    @GetMapping("/milestones")
    @Operation(summary = "QPortal List Milestones API", description = "Return a list of milestones from QPortal.", tags = "qportal")
    Response<List<MilestoneLookup>> listMilestones();

    @GetMapping("/v2/milestones")
    @Operation(summary = "QPortal List Milestones API", description = "Return a list of milestones from QPortal.", tags = "qportal")
    Response<List<MilestoneLookup>> listMilestones(@RequestParam(value = "per_page", defaultValue = "10000") @Min(1) @Max(10000) final int perPage,
                                         @RequestParam(value = "page", defaultValue = "1") @Min(1) @Max(Integer.MAX_VALUE) final int page,
                                         @RequestParam(required = false, value = "key") @Size(min = 1, max = 256) final String key);

    @GetMapping("/milestone-codes")
    @Operation(summary = "QPortal List Milestone Codes API", description = "Return a list of milestone codes from QPortal.", tags = "qportal")
    Response<List<MilestoneResponse>> listMilestoneCodes();

    @GetMapping("/cost_types")
    @Operation(summary = "QPortal List Cost Types API", description = "Return list of cost types from QPortal.", tags = "qportal")
    Response<List<CostType>> listCostTypes();

    @GetMapping("/drivers")
    @Operation(summary = "QPortal List Drivers API", description = "Return list of drivers from QPortal.", tags = "qportal")
    Response<List<Driver>> listDrivers();

    @GetMapping("/currencies")
    @Operation(summary = "QPortal List Currencies API", description = "Return list of currencies from QPortal.", tags = "qportal")
    Response<List<Currency>> listCurrencies();

    @GetMapping("/vehicle-types")
    @Operation(summary = "QPortal List Vehicle Types API", description = "Return list of vehicle types from QPortal.", tags = "qportal")
    Response<List<VehicleType>> listVehicleTypes();
}
