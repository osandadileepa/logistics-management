package com.quincus.shipment.impl.mapper.qportal;

import com.quincus.qportal.model.QPortalPackageType;
import com.quincus.shipment.api.constant.PackageTypeMetricUnit;
import com.quincus.shipment.api.domain.PackageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Spy;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

class QPortalPackageTypeMapperTest {

    @Spy
    private QPortalPackageTypeMapper mapper = Mappers.getMapper(QPortalPackageTypeMapper.class);

    @Test
    @DisplayName("given qPortalPackageType when toPackageType then return converted PackageType")
    void shouldConvertToPackageType() {
        QPortalPackageType qPortalPackageType = new QPortalPackageType();
        qPortalPackageType.setMaxNetWeightUnit("Kilogram");
        qPortalPackageType.setMaxGrossWeightUnit("Inches");
        qPortalPackageType.setInternalVolumeUnit("Pound");
        qPortalPackageType.setInternalVolume(null);
        QPortalPackageType.Dimension dimension = new QPortalPackageType.Dimension();
        dimension.setHeight(BigDecimal.valueOf(10.0));
        dimension.setLength(BigDecimal.valueOf(10.123454));
        dimension.setWidth(BigDecimal.valueOf(10.00100));
        dimension.setMetric("Centimeters");
        qPortalPackageType.setDimension(dimension);

        PackageType packageType = mapper.toPackageType(qPortalPackageType);

        assertThat(packageType.getMaxNetWeightUnit()).isEqualTo(PackageTypeMetricUnit.KG);
        assertThat(packageType.getMaxGrossWeightUnit()).isEqualTo(PackageTypeMetricUnit.IN);
        assertThat(packageType.getInternalVolumeUnit()).isEqualTo(PackageTypeMetricUnit.LB);
        assertThat(packageType.getDimension().getMetric()).isEqualTo(PackageTypeMetricUnit.CM);
        assertThat(packageType.getDimension().getHeight()).isEqualTo(BigDecimal.valueOf(10.0).setScale(1, RoundingMode.FLOOR));
        assertThat(packageType.getDimension().getLength()).isEqualTo(BigDecimal.valueOf(10.123).setScale(3, RoundingMode.FLOOR));
        assertThat(packageType.getDimension().getWidth()).isEqualTo(BigDecimal.valueOf(10.001).setScale(3, RoundingMode.FLOOR));
        assertThat(packageType.getInternalVolume()).isNull();
    }

}