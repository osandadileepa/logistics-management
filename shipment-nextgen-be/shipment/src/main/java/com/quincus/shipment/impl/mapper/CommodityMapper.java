package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.domain.Commodity;
import com.quincus.shipment.impl.repository.entity.CommodityEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.NONE)
public class CommodityMapper {

    public static CommodityEntity mapDomainToEntity(Commodity commodityDomain) {
        CommodityEntity commodityEntity = new CommodityEntity();
        commodityEntity.setId(commodityDomain.getId());
        commodityEntity.setExternalId(commodityDomain.getExternalId());
        commodityEntity.setName(commodityDomain.getName());
        commodityEntity.setQuantity(commodityDomain.getQuantity());
        commodityEntity.setValue(commodityDomain.getValue());
        commodityEntity.setDescription(commodityDomain.getDescription());
        commodityEntity.setCode(commodityDomain.getCode());
        commodityEntity.setHsCode(commodityDomain.getHsCode());
        commodityEntity.setNote(commodityDomain.getNote());
        commodityEntity.setPackagingType(commodityDomain.getPackagingType());

        return commodityEntity;
    }

    public static Commodity mapEntityToDomain(CommodityEntity commodityEntity) {
        Commodity commodityDomain = new Commodity();
        commodityDomain.setId(commodityEntity.getId());
        commodityDomain.setExternalId(commodityEntity.getExternalId());
        commodityDomain.setName(commodityEntity.getName());
        commodityDomain.setQuantity(commodityEntity.getQuantity());
        commodityDomain.setValue(commodityEntity.getValue());
        commodityDomain.setDescription(commodityEntity.getDescription());
        commodityDomain.setCode(commodityEntity.getCode());
        commodityDomain.setHsCode(commodityEntity.getHsCode());
        commodityDomain.setNote(commodityEntity.getNote());
        commodityDomain.setPackagingType(commodityEntity.getPackagingType());
        return commodityDomain;
    }

    public static List<CommodityEntity> mapDomainListToEntityListCommodity(List<Commodity> commodityDomainList) {
        List<CommodityEntity> commodityEntityList = new ArrayList<>(commodityDomainList.size());
        for (Commodity commodity : commodityDomainList) {
            commodityEntityList.add(mapDomainToEntity(commodity));
        }

        return commodityEntityList;
    }

    public static List<Commodity> mapEntityListToDomainListCommodity(List<CommodityEntity> commodityEntityEntityList) {
        List<Commodity> commodityDomainList = new ArrayList<>(commodityEntityEntityList.size());
        for (CommodityEntity commodity : commodityEntityEntityList) {
            commodityDomainList.add(mapEntityToDomain(commodity));
        }

        return commodityDomainList;
    }
}
