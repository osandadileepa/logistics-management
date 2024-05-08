package com.quincus.networkmanagement.api.filter;

import com.quincus.networkmanagement.api.constant.TransportType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ConnectionSearchFilter extends SearchFilter {
    private String connectionCode;
    private String vendorId;
    private TransportType transportType;
    private String departureNodeId;
    private String arrivalNodeId;
    private String[] tags;
}
