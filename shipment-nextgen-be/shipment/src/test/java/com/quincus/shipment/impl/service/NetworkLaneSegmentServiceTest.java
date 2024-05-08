package com.quincus.shipment.impl.service;

import com.quincus.shipment.impl.mapper.NetworkLaneSegmentLocationTupleMapper;
import com.quincus.shipment.impl.mapper.NetworkLaneSegmentPartnerTupleMapper;
import com.quincus.shipment.impl.mapper.NetworkLaneSegmentTupleMapper;
import com.quincus.shipment.impl.repository.NetworkLaneSegmentRepository;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import com.quincus.shipment.impl.repository.entity.NetworkLaneSegmentEntity;
import com.quincus.shipment.impl.repository.entity.PartnerEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.Tuple;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NetworkLaneSegmentServiceTest {

    @InjectMocks
    private NetworkLaneSegmentService service;
    @Mock
    private NetworkLaneSegmentRepository repository;
    @Mock
    private NetworkLaneSegmentTupleMapper networkLaneSegmentTupleMapper;
    @Mock
    private NetworkLaneSegmentLocationTupleMapper networkLaneSegmentLocationTupleMapper;
    @Mock
    private NetworkLaneSegmentPartnerTupleMapper networkLaneSegmentPartnerTupleMapper;

    @Test
    void givenIdsWithNoResult_findAllByNetworkLaneIdsIn_NoExceptionAndReturnEmptyResults() {
        List<String> ids = List.of("1", "2", "3", "4");
        List<Tuple> tupleResults = new ArrayList<>();
        when(repository.findAllByNetworkLaneIdsIn(ids)).thenReturn(tupleResults);
        List<NetworkLaneSegmentEntity> networkLaneSegmentList = service.findByNetworkLaneIds(ids);
        verify(repository, times(1)).findAllByNetworkLaneIdsIn(ids);
        assertThat(networkLaneSegmentList).isNotNull().isEmpty();
    }

    @Test
    void givenIdsWithResult_findAllByNetworkLaneIdsIn_ReturnMappedTupleToNetworkLaneSegmentEntities() {
        //GIVEN:
        List<String> ids = List.of("1", "2", "3", "4");
        List<Tuple> tupleResults = new ArrayList<>();
        tupleResults.add(mock(Tuple.class));
        tupleResults.add(mock(Tuple.class));
        when(repository.findAllByNetworkLaneIdsIn(ids)).thenReturn(tupleResults);
        when(networkLaneSegmentTupleMapper.toEntity(tupleResults.get(0))).thenReturn(mock(NetworkLaneSegmentEntity.class));
        when(networkLaneSegmentTupleMapper.toEntity(tupleResults.get(1))).thenReturn(mock(NetworkLaneSegmentEntity.class));
        when(networkLaneSegmentLocationTupleMapper.toEntity(tupleResults.get(0), "start")).thenReturn(mock(LocationHierarchyEntity.class));
        when(networkLaneSegmentLocationTupleMapper.toEntity(tupleResults.get(0), "end")).thenReturn(mock(LocationHierarchyEntity.class));
        when(networkLaneSegmentLocationTupleMapper.toEntity(tupleResults.get(1), "start")).thenReturn(mock(LocationHierarchyEntity.class));
        when(networkLaneSegmentLocationTupleMapper.toEntity(tupleResults.get(1), "end")).thenReturn(mock(LocationHierarchyEntity.class));
        when(networkLaneSegmentPartnerTupleMapper.toEntity(tupleResults.get(0))).thenReturn(mock(PartnerEntity.class));
        when(networkLaneSegmentPartnerTupleMapper.toEntity(tupleResults.get(1))).thenReturn(mock(PartnerEntity.class));

        //WHEN:
        List<NetworkLaneSegmentEntity> networkLaneSegmentList = service.findByNetworkLaneIds(ids);

        //THEN:
        verify(repository, times(1)).findAllByNetworkLaneIdsIn(ids);
        assertThat(networkLaneSegmentList).isNotNull().isNotEmpty().hasSize(2);
        assertPartnersIsSet(networkLaneSegmentList);
        assertStartAndEndLocationHierarchyIsSet(networkLaneSegmentList);
    }

    private void assertPartnersIsSet(List<NetworkLaneSegmentEntity> segmentEntities) {
        segmentEntities.forEach(segmentEntity -> verify(segmentEntity, times(1)).setPartner(any()));
    }

    private void assertStartAndEndLocationHierarchyIsSet(List<NetworkLaneSegmentEntity> segmentEntities) {
        segmentEntities.forEach(segmentEntity -> {
            verify(segmentEntity, times(1)).setStartLocationHierarchy(any());
            verify(segmentEntity, times(1)).setEndLocationHierarchy(any());
        });
    }
}