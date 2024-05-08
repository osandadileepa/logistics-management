package com.quincus.apigateway.api.dto;

import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Data
public class APIGPackage implements Comparable<APIGPackage> {
    private String packageNo;
    private String additionalData1;
    private String description;
    private String milestone;
    private String milestoneClassification;
    private String reasonCode;
    private String reason;
    private String timestamp;
    private String transportationType;
    private String waybillNumber;
    private String packagingType;
    private String uom;
    private BigDecimal weight;
    private BigDecimal volume;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    private String currencyType;
    private BigDecimal packageValue;
    private BigDecimal codAmt;
    private BigDecimal actualCollectedAmt;
    private String remark;
    private List<APIGCommodity> commodities;

    @Override
    public int compareTo(APIGPackage other) {
        if (packageNo != null) {
            int cmp = ObjectUtils.compare(packageNo, other.packageNo);
            if (cmp != 0) {
                return cmp;
            }
        }
        return ObjectUtils.compare(additionalData1, other.additionalData1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        APIGPackage that = (APIGPackage) o;
        return Objects.equals(packageNo, that.packageNo) &&
                Objects.equals(additionalData1, that.additionalData1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageNo, additionalData1);
    }
}
