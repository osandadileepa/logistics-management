package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.domain.Package;
import com.quincus.shipment.impl.repository.entity.PackageEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;

@NoArgsConstructor(access = AccessLevel.NONE)
public class PackageMapper {


    public static PackageEntity toEntity(Package packageDomain) {
        if (packageDomain == null) {
            return null;
        }
        PackageEntity packageEntity = new PackageEntity();
        packageEntity.setId(packageDomain.getId());
        packageEntity.setRefId(packageDomain.getRefId());
        packageEntity.setTotalValue(packageDomain.getTotalValue());
        packageEntity.setCurrency(packageDomain.getCurrency());
        packageEntity.setType(packageDomain.getType());
        packageEntity.setTypeRefId(packageDomain.getTypeRefId());
        packageEntity.setValue(packageDomain.getValue());
        packageEntity.setReadyTime(packageDomain.getReadyTime());
        packageEntity.setDimension(PackageDimensionMapper.mapDomainToEntity(packageDomain.getDimension()));
        packageEntity.setCommodities(CommodityMapper.mapDomainListToEntityListCommodity(packageDomain.getCommodities()));
        packageEntity.setPricingInfo(packageDomain.getPricingInfo());
        packageEntity.setCode(packageDomain.getCode());
        packageEntity.setTotalItemsCount(packageDomain.getTotalItemsCount());
        packageEntity.setSource(packageDomain.getSource());
        return packageEntity;
    }

    public static Package toDomain(PackageEntity packageEntity) {
        if (packageEntity == null) {
            return null;
        }
        Package packageDomain = new Package();
        packageDomain.setId(packageEntity.getId());
        packageDomain.setRefId(packageEntity.getRefId());
        packageDomain.setType(packageEntity.getType());
        packageDomain.setTypeRefId(packageEntity.getTypeRefId());
        packageDomain.setCurrency(packageEntity.getCurrency());
        packageDomain.setTotalValue(packageEntity.getTotalValue());
        packageDomain.setValue(packageEntity.getValue());
        packageDomain.setReadyTime(packageEntity.getReadyTime());
        packageDomain.setDimension(PackageDimensionMapper.mapEntityToDomain(packageEntity.getDimension()));
        if (CollectionUtils.isNotEmpty(packageEntity.getCommodities())) {
            packageDomain.setCommodities(CommodityMapper.mapEntityListToDomainListCommodity(packageEntity.getCommodities()));
        }
        packageDomain.setPricingInfo(packageEntity.getPricingInfo());
        packageDomain.setCode(packageEntity.getCode());
        packageDomain.setTotalItemsCount(packageEntity.getTotalItemsCount());
        packageDomain.setSource(packageEntity.getSource());
        return packageDomain;
    }
}
