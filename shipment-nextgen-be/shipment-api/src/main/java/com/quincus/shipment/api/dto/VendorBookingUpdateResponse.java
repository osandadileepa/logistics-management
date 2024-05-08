package com.quincus.shipment.api.dto;

import com.quincus.shipment.api.domain.PackageJourneySegment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VendorBookingUpdateResponse {
    private PackageJourneySegment packageJourneySegment;

}
