package com.quincus.shipment.impl.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.constant.TriggeredFrom;
import com.quincus.shipment.api.domain.Commodity;
import com.quincus.shipment.api.domain.Package;
import com.quincus.shipment.api.domain.PackageDimension;
import com.quincus.shipment.api.domain.PricingInfo;
import com.quincus.shipment.impl.repository.entity.CommodityEntity;
import com.quincus.shipment.impl.repository.entity.PackageDimensionEntity;
import com.quincus.shipment.impl.repository.entity.PackageEntity;
import com.quincus.shipment.impl.test_utils.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PackageMapperTest {

    private final TestUtil testUtil = TestUtil.getInstance();

    private final ObjectMapper objectMapper = testUtil.getObjectMapper();

    @Test
    void mapDomainToEntity_packageDomain_shouldReturnPackageEntity() {
        Package domain = new Package();
        domain.setId("PACKAGE1");
        domain.setRefId("PACKAGE1-FROM-OM");
        domain.setType("PACKAGE_TYPE1");
        domain.setCurrency("USD");
        domain.setTotalValue(new BigDecimal("5.55"));
        domain.setValue("6.66");
        domain.setReadyTime(LocalDateTime.now());
        domain.setDimension(new PackageDimension());
        domain.setCommodities(List.of(new Commodity()));
        domain.setPricingInfo(new PricingInfo());
        domain.setSource(TriggeredFrom.SHP);

        PackageDimension dimensionDomain = new PackageDimension();
        dimensionDomain.setId("TMP");
        domain.setDimension(dimensionDomain);

        Commodity commodityDomain = new Commodity();
        commodityDomain.setId("TMP");
        domain.setCommodities(List.of(commodityDomain));

        PricingInfo pricingInfoDomain = new PricingInfo();
        pricingInfoDomain.setId("TMP");
        domain.setPricingInfo(pricingInfoDomain);

        final PackageEntity entity = PackageMapper.toEntity(domain);

        assertThat(entity)
                .usingRecursiveComparison()
                .ignoringFields("modifyTime", "createTime", "version", "commodities", "dimension", "pricingInfo", "organizationId")
                .isEqualTo(domain);
        assertThat(entity.getCommodities()).isNotNull();
        assertThat(entity.getDimension()).isNotNull();
        assertThat(entity.getPricingInfo()).isNotNull();
        assertThat(entity.getSource()).isNotNull();
    }

    @Test
    void mapDomainToEntity_packageDomainNull_shouldReturnNull() {
        assertThat(PackageMapper.toEntity(null)).isNull();
    }

    @Test
    void mapEntityToDomain_packageEntity_shouldReturnPackageDomain() {
        PackageEntity entity = new PackageEntity();
        entity.setId("PCK-1");
        entity.setRefId("package-1-from-domain");
        entity.setTotalValue(new BigDecimal("0.11"));
        entity.setCurrency("USD");
        entity.setType("P-TYPE1");
        entity.setValue("12.34");
        entity.setReadyTime(LocalDateTime.now());
        entity.setSource(TriggeredFrom.SHP);

        PackageDimensionEntity dimensionEntity = new PackageDimensionEntity();
        dimensionEntity.setId("temp");
        entity.setDimension(dimensionEntity);

        CommodityEntity commodityEntity = new CommodityEntity();
        commodityEntity.setId("temp");
        entity.setCommodities(List.of(commodityEntity));

        PricingInfo pricingInfo = new PricingInfo();
        pricingInfo.setId("temp");
        entity.setPricingInfo(pricingInfo);

        final Package domain = PackageMapper.toDomain(entity);

        assertThat(domain)
                .usingRecursiveComparison()
                .ignoringFields("commodities", "dimension", "pricingInfo", "organizationId")
                .isEqualTo(entity);
        assertThat(domain.getCommodities()).isNotNull();
        assertThat(domain.getDimension()).isNotNull();
        assertThat(domain.getPricingInfo()).isNotNull();
        assertThat(domain.getSource()).isNotNull();
    }

    @Test
    void mapEntityToDomain_packageEntityNull_shouldReturnNull() {
        assertThat(PackageMapper.toDomain(null)).isNull();
    }
}
