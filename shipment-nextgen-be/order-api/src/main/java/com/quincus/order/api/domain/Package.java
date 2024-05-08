package com.quincus.order.api.domain;

import lombok.Data;

import java.util.List;

@Data
public class Package {
    private String id;
    private String orderId;
    private int itemsCount;
    private String description;
    private String uldId;
    private String createdAt;
    private String shipmentIdLabel;
    private String note;
    private String measurementUnits;
    private String packageType;
    private Packaging packaging;
    private double length;
    private double width;
    private double height;
    private List<Object> milestones;
    private String currentLatitude;
    private String currentLongitude;
    private double valueOfGoods;
    private String insuranceType;
    private double grossWeight;
    private double volumeWeight;
    private double chargeableWeight;
    private double commoditiesFees;
    private double packageTypeFee;
    private List<Commodity> commodities;
    private List<CommoditiesPackage> commoditiesPackages;
    private String code;
    private String additionalData1;
    private String rootPackageId;
    private String customPackageType;
    private List<String> allTagsList;
    private PackageAddon packageAddon;
    private PackageInsurance packageInsurance;
}
