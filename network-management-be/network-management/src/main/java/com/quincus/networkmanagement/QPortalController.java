package com.quincus.networkmanagement;

import com.quincus.networkmanagement.api.domain.Currency;
import com.quincus.networkmanagement.api.domain.Facility;
import com.quincus.networkmanagement.api.domain.Partner;
import com.quincus.networkmanagement.api.domain.VehicleType;
import com.quincus.web.common.model.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/qportal")
@Tag(name = "qportal", description = "This endpoint acts as a gateway for QPortal API")
public interface QPortalController {

    @GetMapping("/facilities")
    @Operation(summary = "QPortal List Facilities API", description = "Return list of facilities from QPortal", tags = "qportal")
    Response<List<Facility>> listFacilities();

    @GetMapping("/vendors")
    @Operation(summary = "QPortal List Vendors API", description = "Return list of vendors from QPortal", tags = "qportal")
    Response<List<Partner>> listPartners();

    @GetMapping("/currencies")
    @Operation(summary = "QPortal List Currencies API", description = "Return list of currencies from QPortal", tags = "qportal")
    Response<List<Currency>> listCurrencies();

    @GetMapping("/vehicle-types")
    @Operation(summary = "QPortal List Vehicle Types API", description = "Return list of vehicle types from QPortal", tags = "qportal")
    Response<List<VehicleType>> listVehicleTypes();

    @GetMapping("/tags")
    @Operation(summary = "QPortal List Tags API", description = "Return list of tags from QPortal", tags = "qportal")
    Response<List<String>> listTags();

}
