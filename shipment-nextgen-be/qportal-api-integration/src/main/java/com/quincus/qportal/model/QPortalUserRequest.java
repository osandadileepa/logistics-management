package com.quincus.qportal.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class QPortalUserRequest {

    private String query;
    private List<String> accessiblePartnerIds;
    private boolean isDriver;
    private boolean existent = true;

}
