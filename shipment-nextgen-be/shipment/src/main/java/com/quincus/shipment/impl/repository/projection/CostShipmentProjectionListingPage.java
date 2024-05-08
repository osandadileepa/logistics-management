package com.quincus.shipment.impl.repository.projection;

import com.quincus.shipment.impl.repository.entity.AddressEntity;
import com.quincus.shipment.impl.repository.entity.OrderEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity_;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity;
import com.quincus.shipment.impl.repository.entity.component.BaseEntity_;
import com.quincus.shipment.impl.service.AddressService;
import com.quincus.shipment.impl.service.PackageJourneySegmentService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.nonNull;

/**
 * This class is specific to Cost Shipments Listing Page.
 */
@Service
public class CostShipmentProjectionListingPage extends BaseProjection<ShipmentEntity> {

    private static final List<String> fields = List.of(BaseEntity_.ID, ShipmentEntity_.SHIPMENT_TRACKING_ID, ShipmentEntity_.PARTNER_ID,
            BaseEntity_.CREATE_TIME, ShipmentEntity_.ORIGIN_ID, ShipmentEntity_.DESTINATION_ID, ShipmentEntity_.SHIPMENT_JOURNEY_ID, ShipmentEntity_.ORDER_ID);
    private final AddressService addressService;
    private final PackageJourneySegmentService packageJourneySegmentService;

    @Autowired
    public CostShipmentProjectionListingPage(EntityManager entityManager,
                                             AddressService addressService,
                                             PackageJourneySegmentService packageJourneySegmentService) {
        super(entityManager, ShipmentEntity.class);
        this.addressService = addressService;
        this.packageJourneySegmentService = packageJourneySegmentService;
    }

    public List<ShipmentEntity> findAllWithPagination(Specification<ShipmentEntity> specs, Pageable pageable) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = builder.createTupleQuery();
        Root<ShipmentEntity> root = applySpecToCriteria(query, builder, specs);
        List<Selection<?>> selections = getSelections(root, fields);
        query.multiselect(selections);
        applySorting(builder, query, root, pageable);
        List<Tuple> result = getPageableResultList(query, pageable);
        return createShipmentEntityListFromResult(result);
    }

    public List<ShipmentEntity> createShipmentEntityListFromResult(List<Tuple> tuples) {
        if (CollectionUtils.isEmpty(tuples)) return Collections.emptyList();
        List<ShipmentEntity> shipmentEntities = new ArrayList<>();
        List<AddressEntity> addressEntities = addressService.getAddressByIds(getAllAddressIds(tuples));
        List<PackageJourneySegmentEntity> segments = packageJourneySegmentService.findByShipmentJourneyIds(getAllJourneyIds(tuples));
        tuples.forEach(e -> {
            ShipmentEntity entity = enrichShipmentEntity(e, addressEntities);
            enrichShipmentJourney(segments, entity.getShipmentJourney());
            shipmentEntities.add(entity);
        });
        return shipmentEntities;
    }

    private void enrichShipmentJourney(List<PackageJourneySegmentEntity> segments, ShipmentJourneyEntity journeyEntity) {
        List<PackageJourneySegmentEntity> filteredSegments = segments
                .stream()
                .filter(pjs -> StringUtils.equals(pjs.getShipmentJourneyId(), journeyEntity.getId()))
                .toList();
        if (CollectionUtils.isNotEmpty(filteredSegments)) {
            journeyEntity.removeAllPackageJourneySegments();
            journeyEntity.addAllPackageJourneySegments(filteredSegments);
        }
    }

    private List<String> getAllJourneyIds(List<Tuple> result) {
        if (CollectionUtils.isEmpty(result)) return Collections.emptyList();
        List<String> ids = new ArrayList<>();
        result.forEach(tuple -> ids.add(tuple.get(ShipmentEntity_.SHIPMENT_JOURNEY_ID, String.class)));
        return ids;
    }

    private List<String> getAllAddressIds(List<Tuple> result) {
        if (CollectionUtils.isEmpty(result)) return Collections.emptyList();
        List<String> ids = new ArrayList<>();
        result.forEach(tuple -> {
            ids.add(tuple.get(ShipmentEntity_.ORIGIN_ID, String.class));
            ids.add(tuple.get(ShipmentEntity_.DESTINATION_ID, String.class));
        });
        return ids;
    }

    private ShipmentEntity enrichShipmentEntity(Tuple tuple, List<AddressEntity> addressEntities) {
        ShipmentEntity entity = convertTupleToShipmentEntity(tuple);
        assignOrigin(entity, addressEntities, entity.getOrigin().getId());
        assignDestination(entity, addressEntities, entity.getDestination().getId());
        return entity;
    }

    private ShipmentEntity convertTupleToShipmentEntity(Tuple tuple) {
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setId(tuple.get(BaseEntity_.ID, String.class));
        shipmentEntity.setPartnerId(tuple.get(ShipmentEntity_.PARTNER_ID, String.class));
        shipmentEntity.setShipmentTrackingId(tuple.get(ShipmentEntity_.SHIPMENT_TRACKING_ID, String.class));
        shipmentEntity.setOrigin(createAddressEntityAndAssignId(tuple.get(ShipmentEntity_.ORIGIN_ID, String.class)));
        shipmentEntity.setDestination(createAddressEntityAndAssignId(tuple.get(ShipmentEntity_.DESTINATION_ID, String.class)));
        shipmentEntity.setShipmentJourney(createShipmentJourneyEntityAndAssignId(tuple.get(ShipmentEntity_.SHIPMENT_JOURNEY_ID, String.class)));
        shipmentEntity.setOrder(createOrderEntityWithId(tuple.get(ShipmentEntity_.ORDER_ID, String.class)));
        return shipmentEntity;
    }

    private OrderEntity createOrderEntityWithId(String id) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(id);
        return orderEntity;
    }

    private ShipmentJourneyEntity createShipmentJourneyEntityAndAssignId(String id) {
        ShipmentJourneyEntity journeyEntity = new ShipmentJourneyEntity();
        journeyEntity.setId(id);
        return journeyEntity;
    }

    private AddressEntity createAddressEntityAndAssignId(String id) {
        AddressEntity address = new AddressEntity();
        address.setId(id);
        return address;
    }

    private void assignOrigin(ShipmentEntity shipmentEntity, List<AddressEntity> addresses, String addressId) {
        AddressEntity entity = findAddress(addresses, addressId);
        if (nonNull(entity)) {
            shipmentEntity.setOrigin(entity);
        }
    }

    private void assignDestination(ShipmentEntity shipmentEntity, List<AddressEntity> addresses, String addressId) {
        AddressEntity entity = findAddress(addresses, addressId);
        if (nonNull(entity)) {
            shipmentEntity.setDestination(entity);
        }
    }

    private AddressEntity findAddress(List<AddressEntity> addresses, String addressId) {
        if (CollectionUtils.isEmpty(addresses) || StringUtils.isEmpty(addressId)) return null;
        return addresses
                .stream()
                .filter(a -> StringUtils.equals(a.getId(), addressId))
                .findAny().orElse(null);
    }

}
