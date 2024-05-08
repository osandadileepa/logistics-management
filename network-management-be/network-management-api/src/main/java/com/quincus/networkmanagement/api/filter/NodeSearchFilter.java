package com.quincus.networkmanagement.api.filter;

import com.quincus.networkmanagement.api.constant.NodeType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
public class NodeSearchFilter extends SearchFilter {
    private String nodeCode;
    private NodeType nodeType;
    private String facilityId;
    private String vendorId;
    private String[] tags;
}
