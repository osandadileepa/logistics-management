package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.domain.Commodity;
import com.quincus.shipment.impl.repository.entity.CommodityEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CommodityMapperTest {

    @Test
    void mapDomainToEntity_commodityDomain_shouldReturnCommodityEntity() {
        Commodity domain = new Commodity();
        domain.setId("COMMODITY-ID");
        domain.setExternalId("EXT-ID");
        domain.setName("NAME");
        domain.setQuantity(3L);
        domain.setValue(new BigDecimal("8.88"));
        domain.setDescription("DESC");
        domain.setHsCode("T01");
        domain.setCode("CODE");
        domain.setNote("NOTE");
        domain.setPackagingType("TYPE");

        final CommodityEntity entity = CommodityMapper.mapDomainToEntity(domain);

        assertThat(entity)
                .usingRecursiveComparison()
                .ignoringFields("modifyTime", "createTime", "version", "organizationId")
                .isEqualTo(domain);
    }

    @Test
    void mapEntityToDomain_commodityEntity_shouldReturnCommodityDomain() {
        CommodityEntity entity = new CommodityEntity();
        entity.setId("id");
        entity.setExternalId("ext-id");
        entity.setName("name");
        entity.setQuantity(1L);
        entity.setValue(new BigDecimal("1.80"));
        entity.setDescription("DESC");
        entity.setHsCode("T01");
        entity.setCode("CODE");
        entity.setNote("NOTE");
        entity.setPackagingType("TYPE");

        final Commodity domain = CommodityMapper.mapEntityToDomain(entity);

        assertThat(domain)
                .usingRecursiveComparison()
                .ignoringFields("modifyTime", "createTime", "version", "organizationId")
                .isEqualTo(entity);
    }

    @Test
    void mapDomainListToEntityListCommodity_commodityDomainList_shouldReturnCommodityEntityList() {
        ArrayList<Commodity> domainList = new ArrayList<>();
        domainList.add(new Commodity());
        domainList.add(new Commodity());
        domainList.get(0).setId("ID1");
        domainList.get(0).setExternalId("EXT-ID1");
        domainList.get(0).setName("NAME1");
        domainList.get(0).setQuantity(11L);
        domainList.get(0).setValue(new BigDecimal("11.40"));
        domainList.get(1).setId("ID2");
        domainList.get(1).setExternalId("EXT-ID2");
        domainList.get(1).setName("NAME2");
        domainList.get(1).setQuantity(12L);
        domainList.get(1).setValue(new BigDecimal("12.40"));
        domainList.get(1).setDescription("DESC");
        domainList.get(1).setHsCode("T01");
        domainList.get(1).setCode("CODE");
        domainList.get(1).setNote("NOTE");
        domainList.get(1).setPackagingType("TYPE");

        final List<CommodityEntity> entityList = CommodityMapper.mapDomainListToEntityListCommodity(domainList);
        assertThat(entityList)
                .usingRecursiveComparison()
                .ignoringFields("modifyTime", "createTime", "version", "organizationId")
                .isEqualTo(domainList);
    }

    @Test
    void mapEntityListToDomainListCommodity_commodityEntityList_shouldReturnCommodityDomainList() {
        ArrayList<CommodityEntity> entityList = new ArrayList<>();
        entityList.add(new CommodityEntity());
        entityList.add(new CommodityEntity());
        entityList.get(0).setId("id1");
        entityList.get(0).setExternalId("ext-id1");
        entityList.get(0).setName("name1");
        entityList.get(0).setQuantity(21L);
        entityList.get(0).setValue(new BigDecimal("21.40"));
        entityList.get(1).setId("id2");
        entityList.get(1).setExternalId("ext-id2");
        entityList.get(1).setName("name2");
        entityList.get(1).setQuantity(22L);
        entityList.get(1).setValue(new BigDecimal("22.40"));
        entityList.get(1).setDescription("DESC");
        entityList.get(1).setHsCode("T01");
        entityList.get(1).setCode("CODE");
        entityList.get(1).setNote("NOTE");
        entityList.get(1).setPackagingType("TYPE");

        final List<Commodity> domainList = CommodityMapper.mapEntityListToDomainListCommodity(entityList);
        assertThat(domainList)
                .usingRecursiveComparison()
                .ignoringFields("modifyTime", "createTime", "version")
                .isEqualTo(entityList);
    }
}
