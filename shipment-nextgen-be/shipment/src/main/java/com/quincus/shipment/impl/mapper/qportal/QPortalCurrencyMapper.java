package com.quincus.shipment.impl.mapper.qportal;

import com.quincus.qportal.model.QPortalCurrency;
import com.quincus.shipment.api.domain.Currency;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface QPortalCurrencyMapper {
    Currency toCurrency(QPortalCurrency qPortalCurrency);
}
