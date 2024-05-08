package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.constant.MeasurementUnit;
import com.quincus.shipment.api.domain.PackageDimension;
import com.quincus.shipment.impl.repository.entity.PackageDimensionEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PackageDimensionMapperTest {

    @Test
    void mapDomainToEntity_dimensionDomain_shouldReturnDimensionEntity() {
        PackageDimension domain = new PackageDimension();
        domain.setId("ID1");
        domain.setMeasurementUnit(MeasurementUnit.METRIC);
        domain.setLength(new BigDecimal("0.81"));
        domain.setWidth(new BigDecimal("0.82"));
        domain.setHeight(new BigDecimal("0.83"));
        domain.setVolumeWeight(new BigDecimal("0.91"));
        domain.setGrossWeight(new BigDecimal("0.92"));
        domain.setChargeableWeight(new BigDecimal("0.93"));
        domain.setCustom(true);

        final PackageDimensionEntity entity = PackageDimensionMapper.mapDomainToEntity(domain);

        assertThat(entity)
                .usingRecursiveComparison()
                .ignoringFields("modifyTime", "createTime", "version", "organizationId")
                .isEqualTo(domain);
    }

    @Test
    void mapEntityToDomain_dimensionEntity_shouldReturnDimensionDomain() {
        PackageDimensionEntity entity = new PackageDimensionEntity();
        entity.setId("id1");
        entity.setMeasurementUnit(MeasurementUnit.METRIC);
        entity.setLength(new BigDecimal("1.81"));
        entity.setWidth(new BigDecimal("1.82"));
        entity.setHeight(new BigDecimal("1.83"));
        entity.setVolumeWeight(new BigDecimal("1.91"));
        entity.setGrossWeight(new BigDecimal("1.92"));
        entity.setChargeableWeight(new BigDecimal("1.93"));
        entity.setCustom(true);
        entity.setCreateTime(Instant.now());
        entity.setModifyTime(Instant.now());

        final PackageDimension domain = PackageDimensionMapper.mapEntityToDomain(entity);

        assertThat(domain)
                .usingRecursiveComparison()
                .ignoringFields("modifyDate", "createDate", "version", "organizationId")
                .isEqualTo(entity);
    }

    @Test
    void mapEntityToDomain_dimensionEntity_shouldReturnPackageWithNullDimension() {
        assertThat(PackageDimensionMapper.mapEntityToDomain(null)).isNotNull();
    }
}
