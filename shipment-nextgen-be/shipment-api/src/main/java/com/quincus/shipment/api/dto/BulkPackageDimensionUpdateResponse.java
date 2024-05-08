package com.quincus.shipment.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BulkPackageDimensionUpdateResponse {
    private long totalNumberOfRecords; //TODO --> change processedRecords SHPV2-2572
    private long numberOfSuccess; //TODO --> successfulRecords SHPV2-2572
    private List<PackageDimensionErrorRecord> errorRecord; //TODO errorRecords SHPV2-2572
}
