package com.quincus.order.api.domain;

import lombok.Data;

@Data
public class CommoditiesPackage {
    private String id;
    private String commodityId;
    private String packageId;
    private int itemsCount;
    private String valueOfGoods;
    private String deletedAt;
    private String commodityName;
    private String description;
    private String code;
    private String hsCode;
    private String note;
    private String packagingType;
}
