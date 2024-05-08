package com.quincus.networkmanagement.impl.mapper.qportal;

import com.quincus.networkmanagement.api.domain.Currency;
import com.quincus.qportal.model.QPortalCurrency;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface QPortalCurrencyMapper {
    Currency toCurrency(QPortalCurrency qPortalCurrency);
}
