package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.domain.PackageDimension;
import com.quincus.shipment.impl.repository.entity.PackageDimensionEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.NONE)
public class PackageDimensionMapper {

    public static PackageDimensionEntity mapDomainToEntity(PackageDimension dimensionDomain) {
        PackageDimensionEntity dimensionEntity = new PackageDimensionEntity();
        dimensionEntity.setId(dimensionDomain.getId());
        dimensionEntity.setMeasurementUnit(dimensionDomain.getMeasurementUnit());
        dimensionEntity.setLength(dimensionDomain.getLength());
        dimensionEntity.setWidth(dimensionDomain.getWidth());
        dimensionEntity.setHeight(dimensionDomain.getHeight());
        dimensionEntity.setVolumeWeight(dimensionDomain.getVolumeWeight());
        dimensionEntity.setGrossWeight(dimensionDomain.getGrossWeight());
        dimensionEntity.setChargeableWeight(dimensionDomain.getChargeableWeight());
        dimensionEntity.setCustom(dimensionDomain.isCustom());

        return dimensionEntity;
    }

    public static PackageDimension mapEntityToDomain(PackageDimensionEntity dimensionEntity) {
        PackageDimension dimensionDomain = new PackageDimension();
        if (dimensionEntity != null) {
            dimensionDomain.setId(dimensionEntity.getId());
            dimensionDomain.setMeasurementUnit(dimensionEntity.getMeasurementUnit());
            dimensionDomain.setLength(dimensionEntity.getLength());
            dimensionDomain.setWidth(dimensionEntity.getWidth());
            dimensionDomain.setHeight(dimensionEntity.getHeight());
            dimensionDomain.setVolumeWeight(dimensionEntity.getVolumeWeight());
            dimensionDomain.setGrossWeight(dimensionEntity.getGrossWeight());
            dimensionDomain.setChargeableWeight(dimensionEntity.getChargeableWeight());
            dimensionDomain.setCustom(dimensionEntity.isCustom());
            dimensionDomain.setCreateDate(LocalDateMapper.toLocalDateTime(dimensionEntity.getCreateTime()));
            dimensionDomain.setModifyDate(LocalDateMapper.toLocalDateTime(dimensionEntity.getModifyTime()));
        }
        return dimensionDomain;
    }
}
