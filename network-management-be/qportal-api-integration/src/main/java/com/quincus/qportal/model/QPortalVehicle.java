package com.quincus.qportal.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class QPortalVehicle extends QPortalModel {
    private String partnerId;
    private String partnerName;
    private String partnerCode;
    private String locationId;
    private String locationName;
    private String vehicleTypeId;
    private String vehicleTypeName;
    private String vehicleStatus;
    private String vehicleRegistrationNo;
    private String vehiclesModelsId;
    private String vehiclesModelsName;
    private String primaryDriverId;
    private String primaryDriverName;
    private String secondaryDriverId;
    private String secondaryDriverName;
}
