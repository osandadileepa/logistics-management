package com.quincus.shipment.api.dto;

import com.quincus.shipment.api.constant.PackageDimensionUpdateImportHeader;
import com.quincus.shipment.api.exception.PackageDimensionException;
import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Data
public class BulkPackageDimensionUpdateRequest {
    private PackageDimensionUpdateRequest packageDimensionUpdateRequest;
    private PackageDimensionUpdateResponse packageDimensionUpdateResponse;
    private boolean error;

    public String getErrorMessages() {
        if (StringUtils.isNotEmpty(errorMessages)) {
            return errorMessages;
        } else {
            String[] errors = {shipmentError, packageTypeError, unitError, heightError, widthError, lengthError, weightError};
            String[] trimmedErrors = Arrays.stream(errors)
                    .filter(StringUtils::isNotEmpty)
                    .toArray(String[]::new);
            if (ArrayUtils.isNotEmpty(trimmedErrors)) {
                return String.join(" | ", trimmedErrors);
            }
            return null;
        }
    }

    private String errorMessages;
    private String shipmentTrackingIdCell;
    private String packageTypeCell;
    private String unitCell;
    private String heightCell;
    private String widthCell;
    private String lengthCell;
    private String weightCell;

    private String shipmentError;
    private String packageTypeError;
    private String unitError;
    private String heightError;
    private String widthError;
    private String lengthError;
    private String weightError;

    public BulkPackageDimensionUpdateRequest(String[] cell) {
        if (isNull(cell) || cell.length != PackageDimensionUpdateImportHeader.values().length) {
            throw new PackageDimensionException("Parameter must not be null or empty.");
        }
        this.shipmentTrackingIdCell = cell[0];
        this.packageTypeCell = cell[1];
        this.unitCell = cell[2];
        this.heightCell = cell[3];
        this.widthCell = cell[4];
        this.lengthCell = cell[5];
        this.weightCell = cell[6];
    }

    public boolean isError() {
        return nonNull(getErrorMessages());
    }
}
