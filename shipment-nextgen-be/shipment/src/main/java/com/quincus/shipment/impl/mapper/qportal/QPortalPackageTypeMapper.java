package com.quincus.shipment.impl.mapper.qportal;

import com.quincus.qportal.model.QPortalPackageType;
import com.quincus.shipment.api.constant.PackageTypeMetricUnit;
import com.quincus.shipment.api.domain.PackageType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface QPortalPackageTypeMapper {
    int PACKAGE_DIMENSION_SCALE = 3;

    @Mapping(source = "organisationId", target = "organizationId")
    @Mapping(source = "doorAperture", target = "doorAperture", qualifiedByName = "toPackageTypeDimension")
    @Mapping(source = "internalDimension", target = "internalDimension", qualifiedByName = "toPackageTypeDimension")
    @Mapping(source = "dimension", target = "dimension", qualifiedByName = "toPackageTypeDimension")
    @Mapping(target = "maxNetWeightUnit", qualifiedByName = "toPackageTypeMetricUnit")
    @Mapping(target = "tareWeightUnit", qualifiedByName = "toPackageTypeMetricUnit")
    @Mapping(target = "internalVolumeUnit", qualifiedByName = "toPackageTypeMetricUnit")
    @Mapping(target = "maxGrossWeightUnit", qualifiedByName = "toPackageTypeMetricUnit")
    @Mapping(target = "maxGrossWeight", qualifiedByName = "roundOffToPackageDimensionScale")
    @Mapping(target = "tareWeight", qualifiedByName = "roundOffToPackageDimensionScale")
    @Mapping(target = "internalVolume", qualifiedByName = "roundOffToPackageDimensionScale")
    PackageType toPackageType(QPortalPackageType qPortalPackageType);

    @Named("toPackageTypeMetricUnit")
    default PackageTypeMetricUnit toPackageTypeMetricUnit(String value) {
        return PackageTypeMetricUnit.fromValue(value);
    }

    @Named("toPackageTypeDimension")
    default PackageType.Dimension toPackageTypeDimension(QPortalPackageType.Dimension qPortalDimension) {
        if (qPortalDimension == null) {
            return null;
        }
        PackageType.Dimension packageTypeDimension = new PackageType.Dimension();
        packageTypeDimension.setWidth(roundOffToPackageDimensionScale(qPortalDimension.getWidth()));
        packageTypeDimension.setHeight(roundOffToPackageDimensionScale(qPortalDimension.getHeight()));
        packageTypeDimension.setLength(roundOffToPackageDimensionScale(qPortalDimension.getLength()));
        packageTypeDimension.setMetric(toPackageTypeMetricUnit(qPortalDimension.getMetric()));
        return packageTypeDimension;
    }

    @Named("roundOffToPackageDimensionScale")
    default BigDecimal roundOffToPackageDimensionScale(BigDecimal bigDecimalValue) {
        if (bigDecimalValue == null || bigDecimalValue.stripTrailingZeros().scale() <= 0) {
            return bigDecimalValue;
        }
        return bigDecimalValue.setScale(PACKAGE_DIMENSION_SCALE, RoundingMode.HALF_DOWN);
    }

}
