package com.quincus.shipment.impl.service;

import com.quincus.shipment.impl.mapper.NetworkLaneSegmentLocationTupleMapper;
import com.quincus.shipment.impl.mapper.NetworkLaneSegmentPartnerTupleMapper;
import com.quincus.shipment.impl.mapper.NetworkLaneSegmentTupleMapper;
import com.quincus.shipment.impl.repository.NetworkLaneSegmentRepository;
import com.quincus.shipment.impl.repository.entity.NetworkLaneSegmentEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.persistence.Tuple;
import java.util.Collections;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NetworkLaneSegmentService {

    private static final String LOC_START_ALIAS = "start";
    private static final String LOC_END_ALIAS = "end";
    private final NetworkLaneSegmentRepository networkLaneSegmentRepository;
    private final NetworkLaneSegmentTupleMapper networkLaneSegmentTupleMapper;
    private final NetworkLaneSegmentLocationTupleMapper networkLaneSegmentLocationTupleMapper;
    private final NetworkLaneSegmentPartnerTupleMapper networkLaneSegmentPartnerTupleMapper;

    public List<NetworkLaneSegmentEntity> findByNetworkLaneIds(final List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return mapObjectsToNetworkLaneEntities(networkLaneSegmentRepository.findAllByNetworkLaneIdsIn(ids));
    }

    private List<NetworkLaneSegmentEntity> mapObjectsToNetworkLaneEntities(List<Tuple> tuples) {
        return tuples.stream().map(tuple -> {
            NetworkLaneSegmentEntity entity = networkLaneSegmentTupleMapper.toEntity(tuple);
            entity.setPartner(networkLaneSegmentPartnerTupleMapper.toEntity(tuple));
            entity.setStartLocationHierarchy(networkLaneSegmentLocationTupleMapper.toEntity(tuple, LOC_START_ALIAS));
            entity.setEndLocationHierarchy(networkLaneSegmentLocationTupleMapper.toEntity(tuple, LOC_END_ALIAS));
            return entity;
        }).toList();
    }
}
