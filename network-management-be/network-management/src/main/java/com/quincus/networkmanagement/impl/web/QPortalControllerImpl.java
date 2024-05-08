package com.quincus.networkmanagement.impl.web;

import com.quincus.networkmanagement.QPortalController;
import com.quincus.networkmanagement.api.domain.Currency;
import com.quincus.networkmanagement.api.domain.Facility;
import com.quincus.networkmanagement.api.domain.Partner;
import com.quincus.networkmanagement.api.domain.VehicleType;
import com.quincus.networkmanagement.impl.service.QPortalService;
import com.quincus.web.common.model.Response;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RestController
public class QPortalControllerImpl implements QPortalController {

    private final QPortalService qPortalService;

    @Override
    public Response<List<Facility>> listFacilities() {
        return new Response<>(qPortalService.listFacilities());
    }

    @Override
    public Response<List<Partner>> listPartners() {
        return new Response<>(qPortalService.listPartners());
    }

    @Override
    public Response<List<Currency>> listCurrencies() {
        return new Response<>(qPortalService.listCurrencies());
    }

    @Override
    public Response<List<VehicleType>> listVehicleTypes() {
        return new Response<>(qPortalService.listVehicleTypes());
    }

    @Override
    public Response<List<String>> listTags() {
        return new Response<>(qPortalService.listTags());
    }
}
