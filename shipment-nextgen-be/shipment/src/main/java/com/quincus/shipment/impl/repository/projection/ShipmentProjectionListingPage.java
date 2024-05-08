package com.quincus.shipment.impl.repository.projection;

import com.quincus.ext.ListUtil;
import com.quincus.shipment.impl.repository.entity.AddressEntity;
import com.quincus.shipment.impl.repository.entity.AlertEntity;
import com.quincus.shipment.impl.repository.entity.MilestoneEntity;
import com.quincus.shipment.impl.repository.entity.OrderEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity_;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity_;
import com.quincus.shipment.impl.repository.entity.component.BaseEntity_;
import com.quincus.shipment.impl.service.AddressService;
import com.quincus.shipment.impl.service.AlertService;
import com.quincus.shipment.impl.service.MilestoneService;
import com.quincus.shipment.impl.service.OrderService;
import com.quincus.shipment.impl.service.PackageJourneySegmentService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.data.jpa.repository.query.QueryUtils.toOrders;

/**
 * This class is specific to Shipment Listing Page.
 */
@Service
public class ShipmentProjectionListingPage extends BaseProjection<ShipmentEntity> {
    private static final List<String> fields = List.of(BaseEntity_.ID, ShipmentEntity_.SHIPMENT_TRACKING_ID,
            BaseEntity_.CREATE_TIME, ShipmentEntity_.ORDER_ID, ShipmentEntity_.ORIGIN_ID, ShipmentEntity_.DESTINATION_ID,
            ShipmentEntity_.SHIPMENT_JOURNEY_ID);
    private final OrderService orderService;
    private final AddressService addressService;
    private final PackageJourneySegmentService packageJourneySegmentService;
    private final AlertService alertService;
    private final MilestoneService milestoneService;

    @Autowired
    public ShipmentProjectionListingPage(EntityManager entityManager, MilestoneService milestoneService,
                                         OrderService orderService, AddressService addressService,
                                         PackageJourneySegmentService packageJourneySegmentService,
                                         AlertService alertService) {
        super(entityManager, ShipmentEntity.class);
        this.orderService = orderService;
        this.addressService = addressService;
        this.packageJourneySegmentService = packageJourneySegmentService;
        this.alertService = alertService;
        this.milestoneService = milestoneService;
    }

    public List<ShipmentEntity> findAllWithPagination(Specification<ShipmentEntity> specs, Pageable pageable) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = builder.createTupleQuery();
        Root<ShipmentEntity> root = applySpecToCriteria(query, builder, specs);
        query.distinct(false)
                .multiselect(getSelections(root, fields))
                .groupBy(getGroupByExpressions(root));

        applySorting(builder, query, root, pageable);
        List<Tuple> result = getPageableResultList(query, pageable);
        return createShipmentEntityListFromResult(result);
    }

    private List<Expression<?>> getGroupByExpressions(Root<ShipmentEntity> root) {
        List<Expression<?>> orderExpression = new ArrayList<>();
        fields.forEach(field -> orderExpression.add(root.get(field)));
        return orderExpression;
    }


    @Override
    protected void applySorting(CriteriaBuilder builder, CriteriaQuery<Tuple> query, Root<ShipmentEntity> root, Pageable pageable) {
        Sort sort = nonNull(pageable) && pageable.isPaged() ? pageable.getSort() : Sort.unsorted();
        if (sort.isSorted()) {
            List<Order> sortOrders = new ArrayList<>();
            sortOrders.add(buildAlertSortOrder(builder, root));
            sortOrders.addAll(toOrders(sort, root, builder));
            query.orderBy(sortOrders);
        }
    }

    private Order buildAlertSortOrder(CriteriaBuilder builder, Root<ShipmentEntity> root) {
        return builder.desc(builder.count(root.join(ShipmentEntity_.shipmentJourney, JoinType.LEFT)
                .join(ShipmentJourneyEntity_.alerts, JoinType.LEFT).get(BaseEntity_.ID)));
    }

    private void enrichMilestone(List<MilestoneEntity> milestoneEntities, ShipmentEntity shipment) {
        if (CollectionUtils.isEmpty(milestoneEntities) || isNull(shipment)) return;
        MilestoneEntity milestone = milestoneEntities
                .stream()
                .filter(a -> StringUtils.equals(a.getShipmentId(), shipment.getId()))
                .findAny().orElse(null);
        if (nonNull(milestone)) {
            shipment.setMilestoneEvents(Set.of(milestone));
        }
    }

    private ShipmentEntity convertTupleToShipmentEntity(Tuple tuple) {
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setId(tuple.get(BaseEntity_.ID, String.class));
        shipmentEntity.setShipmentTrackingId(tuple.get(ShipmentEntity_.SHIPMENT_TRACKING_ID, String.class));
        shipmentEntity.setOrder(createOrderEntityAndAssignId(tuple.get(ShipmentEntity_.ORDER_ID, String.class)));
        shipmentEntity.setOrigin(createAddressEntityAndAssignId(tuple.get(ShipmentEntity_.ORIGIN_ID, String.class)));
        shipmentEntity.setDestination(createAddressEntityAndAssignId(tuple.get(ShipmentEntity_.DESTINATION_ID, String.class)));
        shipmentEntity.setShipmentJourney(createShipmentJourneyEntityAndAssignId(tuple.get(ShipmentEntity_.SHIPMENT_JOURNEY_ID, String.class)));
        return shipmentEntity;
    }

    private AddressEntity createAddressEntityAndAssignId(String id) {
        AddressEntity address = new AddressEntity();
        address.setId(id);
        return address;
    }

    private OrderEntity createOrderEntityAndAssignId(String id) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(id);
        return orderEntity;
    }

    private ShipmentJourneyEntity createShipmentJourneyEntityAndAssignId(String id) {
        ShipmentJourneyEntity journeyEntity = new ShipmentJourneyEntity();
        journeyEntity.setId(id);
        return journeyEntity;
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

    private List<String> getAllJourneyIds(List<Tuple> result) {
        if (CollectionUtils.isEmpty(result)) return Collections.emptyList();
        List<String> ids = new ArrayList<>();
        result.forEach(tuple -> ids.add(tuple.get(ShipmentEntity_.SHIPMENT_JOURNEY_ID, String.class)));
        return ListUtil.getUniqueEntries(ids);
    }

    private List<String> getAllShipmentIds(List<Tuple> result) {
        if (CollectionUtils.isEmpty(result)) return Collections.emptyList();
        List<String> ids = new ArrayList<>();
        result.forEach(tuple -> ids.add(tuple.get(BaseEntity_.ID, String.class)));
        return ids;
    }

    private List<String> getAllSegmentIds(List<PackageJourneySegmentEntity> segments) {
        List<String> ids = new ArrayList<>();
        segments.forEach(s -> ids.add(s.getId()));
        return ids;
    }

    private List<ShipmentEntity> createShipmentEntityListFromResult(List<Tuple> tuples) {
        if (CollectionUtils.isEmpty(tuples)) return Collections.emptyList();
        List<ShipmentEntity> shipmentEntities = new ArrayList<>();
        List<AddressEntity> addressEntities = addressService.getAddressByIds(getAllAddressIds(tuples));
        List<String> journeyIds = getAllJourneyIds(tuples);
        List<PackageJourneySegmentEntity> segments = packageJourneySegmentService.findByShipmentJourneyIds(journeyIds);
        List<AlertEntity> alertEntities = alertService.findByJourneyIdsAndSegmentIds(journeyIds, getAllSegmentIds(segments));
        List<String> shipmentIds = getAllShipmentIds(tuples);
        List<MilestoneEntity> milestones = milestoneService.findRecentMilestoneByShipmentIds(shipmentIds);
        List<OrderEntity> orders = orderService.findStatusByShipmentIds(shipmentIds);
        tuples.forEach(shipment -> {
            ShipmentEntity enrichedShipment = enrichShipmentEntity(shipment, addressEntities);
            assignOrder(enrichedShipment, orders);
            enrichMilestone(milestones, enrichedShipment);
            enrichShipmentJourney(segments, enrichedShipment.getShipmentJourney());
            if (nonNull(enrichedShipment.getShipmentJourney()) && CollectionUtils.isNotEmpty(enrichedShipment.getShipmentJourney().getPackageJourneySegments())) {
                enrichedShipment.getShipmentJourney().getPackageJourneySegments()
                        .forEach(pjs -> enrichSegmentWithAlerts(alertEntities, pjs));
            }
            enrichJourneyWithAlerts(alertEntities, enrichedShipment.getShipmentJourney());
            shipmentEntities.add(enrichedShipment);
        });
        return shipmentEntities;
    }

    private void enrichJourneyWithAlerts(List<AlertEntity> alertEntities, ShipmentJourneyEntity journey) {
        List<AlertEntity> filteredAlerts = alertEntities
                .stream()
                .filter(a -> StringUtils.equals(a.getShipmentJourneyId(), journey.getId()))
                .toList();
        if (CollectionUtils.isNotEmpty(filteredAlerts)) {
            journey.setAlerts(filteredAlerts);
        }
    }

    private void enrichSegmentWithAlerts(List<AlertEntity> alertEntities, PackageJourneySegmentEntity segment) {
        List<AlertEntity> filteredAlerts = alertEntities
                .stream()
                .filter(alertEntity -> StringUtils.equals(segment.getId(), alertEntity.getPackageJourneySegmentId()))
                .toList();
        if (CollectionUtils.isNotEmpty(filteredAlerts)) {
            segment.setAlerts(filteredAlerts);
        }
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

    private ShipmentEntity enrichShipmentEntity(Tuple tuple, List<AddressEntity> addressEntities) {
        ShipmentEntity entity = convertTupleToShipmentEntity(tuple);
        assignOrigin(entity, addressEntities, entity.getOrigin().getId());
        assignDestination(entity, addressEntities, entity.getDestination().getId());
        return entity;
    }

    private void assignOrder(ShipmentEntity shipmentEntity, List<OrderEntity> orderEntities) {
        OrderEntity orderEntity = findOrder(orderEntities, shipmentEntity.getOrder().getId());
        if (orderEntity != null) {
            shipmentEntity.setOrder(orderEntity);
        }
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

    private OrderEntity findOrder(List<OrderEntity> orderEntities, String orderId) {
        if (CollectionUtils.isEmpty(orderEntities) || StringUtils.isEmpty(orderId)) {
            return null;
        }
        return orderEntities.stream()
                .filter(order -> StringUtils.equals(order.getId(), orderId))
                .findFirst().orElse(null);
    }

    private AddressEntity findAddress(List<AddressEntity> addresses, String addressId) {
        if (CollectionUtils.isEmpty(addresses) || StringUtils.isEmpty(addressId)) return null;
        return addresses
                .stream()
                .filter(a -> StringUtils.equals(a.getId(), addressId))
                .findAny().orElse(null);
    }

}