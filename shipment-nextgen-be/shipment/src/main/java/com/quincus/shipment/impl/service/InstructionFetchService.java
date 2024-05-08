package com.quincus.shipment.impl.service;

import com.quincus.shipment.impl.repository.InstructionRepository;
import com.quincus.shipment.impl.repository.entity.InstructionEntity;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
@Transactional(readOnly = true)
public class InstructionFetchService {

    private final InstructionRepository instructionRepository;
    private final UserDetailsProvider userDetailsProvider;

    public List<InstructionEntity> findBySegmentIds(List<String> segmentIds) {
        return instructionRepository.findAllBySegmentIds(segmentIds, userDetailsProvider.getCurrentOrganizationId());
    }

    public List<InstructionEntity> findByOrderId(String orderId) {
        return instructionRepository.findAllByOrderId(orderId, userDetailsProvider.getCurrentOrganizationId());
    }
}
