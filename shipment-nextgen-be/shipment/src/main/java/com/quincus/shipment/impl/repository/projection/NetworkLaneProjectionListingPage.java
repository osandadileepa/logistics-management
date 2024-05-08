package com.quincus.shipment.impl.repository.projection;

import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import com.quincus.shipment.impl.repository.entity.NetworkLaneEntity;
import com.quincus.shipment.impl.repository.entity.NetworkLaneEntity_;
import com.quincus.shipment.impl.repository.entity.NetworkLaneSegmentEntity;
import com.quincus.shipment.impl.repository.entity.ServiceTypeEntity;
import com.quincus.shipment.impl.repository.entity.component.BaseEntity_;
import com.quincus.shipment.impl.repository.entity.component.MultiTenantEntity_;
import com.quincus.shipment.impl.service.LocationHierarchyService;
import com.quincus.shipment.impl.service.NetworkLaneSegmentService;
import com.quincus.shipment.impl.service.ServiceTypeService;
import org.apache.commons.collections.CollectionUtils;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class is specific to NetworkLane Listing Page.
 */
@Service
public class NetworkLaneProjectionListingPage extends BaseProjection<NetworkLaneEntity> {
    private static final List<String> fields = List.of(BaseEntity_.ID, BaseEntity_.CREATE_TIME,
            NetworkLaneEntity_.SERVICE_TYPE_ID, NetworkLaneEntity_.ORIGIN_ID, NetworkLaneEntity_.DESTINATION_ID, MultiTenantEntity_.ORGANIZATION_ID);
    private final NetworkLaneSegmentService networkLaneSegmentService;
    private final LocationHierarchyService locationHierarchyService;
    private final ServiceTypeService serviceTypeService;

    @Autowired
    public NetworkLaneProjectionListingPage(EntityManager entityManager, NetworkLaneSegmentService networkLaneSegmentService
            , LocationHierarchyService locationHierarchyService, ServiceTypeService serviceTypeService) {
        super(entityManager, NetworkLaneEntity.class);
        this.networkLaneSegmentService = networkLaneSegmentService;
        this.locationHierarchyService = locationHierarchyService;
        this.serviceTypeService = serviceTypeService;
    }

    public List<NetworkLaneEntity> findAllWithPagination(Specification<NetworkLaneEntity> specs, Pageable pageable) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = builder.createTupleQuery();
        Root<NetworkLaneEntity> root = applySpecToCriteria(query, builder, specs);
        List<Selection<?>> selections = getSelections(root, fields);

        query.multiselect(selections);
        applySorting(builder, query, root, pageable);
        List<Tuple> result = getPageableResultList(query, pageable);
        return createNetworkLaneEntityListFromResult(result);
    }

    private List<String> getAllNetworkLaneIds(List<Tuple> result) {
        if (CollectionUtils.isEmpty(result)) return Collections.emptyList();
        return result.stream().map(tuple -> tuple.get(BaseEntity_.ID, String.class)).toList();
    }

    private Set<String> getAllServiceTypeIds(List<Tuple> result) {
        if (CollectionUtils.isEmpty(result)) return Collections.emptySet();
        return result.stream().map(tuple -> tuple.get(NetworkLaneEntity_.SERVICE_TYPE_ID, String.class))
                .collect(Collectors.toSet());
    }

    private Set<String> getAllLocationHierarchyIds(List<Tuple> result) {
        Set<String> locationHierarchyIds = new HashSet<>();
        if (CollectionUtils.isEmpty(result)) return locationHierarchyIds;
        result.forEach(tuple -> {
            locationHierarchyIds.add(tuple.get(NetworkLaneEntity_.ORIGIN_ID, String.class));
            locationHierarchyIds.add(tuple.get(NetworkLaneEntity_.DESTINATION_ID, String.class));
        });
        return locationHierarchyIds;
    }

    private List<NetworkLaneEntity> createNetworkLaneEntityListFromResult(List<Tuple> tuples) {
        if (CollectionUtils.isEmpty(tuples)) return Collections.emptyList();

        Map<String, List<NetworkLaneSegmentEntity>> laneSegmentEntitiesByLaneId = networkLaneSegmentService.findByNetworkLaneIds(getAllNetworkLaneIds(tuples))
                .stream().collect(Collectors.groupingBy(NetworkLaneSegmentEntity::getNetworkLaneId));
        Map<String, LocationHierarchyEntity> locationHierarchyByIds = locationHierarchyService.findAllByIds(getAllLocationHierarchyIds(tuples))
                .stream().collect(Collectors.toMap(LocationHierarchyEntity::getId, Function.identity()));
        Map<String, ServiceTypeEntity> serviceTypeById = serviceTypeService.findAllByIds(getAllServiceTypeIds(tuples))
                .stream().collect(Collectors.toMap(ServiceTypeEntity::getId, Function.identity()));

        List<NetworkLaneEntity> networkLaneEntities = new ArrayList<>();
        tuples.forEach(e -> {
            NetworkLaneEntity entity = maptToNetworkLaneEntity(e);
            entity.setOrigin(locationHierarchyByIds.get(entity.getOriginId()));
            entity.setDestination(locationHierarchyByIds.get(entity.getDestinationId()));
            entity.addAllNetworkLaneSegments(laneSegmentEntitiesByLaneId.get(entity.getId()));
            entity.setServiceType(serviceTypeById.get(entity.getServiceTypeId()));
            networkLaneEntities.add(entity);
        });
        return networkLaneEntities;
    }

    private NetworkLaneEntity maptToNetworkLaneEntity(Tuple tuple) {
        NetworkLaneEntity entity = new NetworkLaneEntity();
        entity.setId(tuple.get(BaseEntity_.ID, String.class));
        entity.setServiceTypeId(tuple.get(NetworkLaneEntity_.SERVICE_TYPE_ID, String.class));
        entity.setDestinationId(tuple.get(NetworkLaneEntity_.DESTINATION_ID, String.class));
        entity.setOriginId(tuple.get(NetworkLaneEntity_.ORIGIN_ID, String.class));
        return entity;
    }
}