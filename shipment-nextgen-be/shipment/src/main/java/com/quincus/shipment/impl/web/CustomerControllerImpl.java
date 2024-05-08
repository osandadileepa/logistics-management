package com.quincus.shipment.impl.web;

import com.quincus.shipment.CustomerController;
import com.quincus.shipment.api.FilterApi;
import com.quincus.shipment.api.filter.Filter;
import com.quincus.shipment.api.filter.FilterResult;
import com.quincus.web.common.model.Response;
import com.quincus.web.common.utility.annotation.LogExecutionTime;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class CustomerControllerImpl implements CustomerController {
    private final FilterApi filterApi;

    @Override
    @LogExecutionTime
    public Response<FilterResult> findCustomers(final int perPage,
                                                final int page,
                                                final String key) {
        Filter filter = new Filter();
        filter.setPage(page);
        filter.setPerPage(perPage);
        if (StringUtils.isNotEmpty(key)) {
            filter.setKey(key.toLowerCase());
        }
        return new Response<>(filterApi.findCustomers(filter));
    }
}
