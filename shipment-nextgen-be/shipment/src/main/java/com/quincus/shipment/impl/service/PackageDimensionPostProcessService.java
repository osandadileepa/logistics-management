package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.MessageApi;
import com.quincus.shipment.api.constant.TriggeredFrom;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.Shipment;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PackageDimensionPostProcessService {

    private final MilestoneService milestoneService;
    private final MessageApi messageApi;

    public void createAndSendDimsAndWeightMilestoneForShipment(Shipment updatedShipment) {
        Milestone milestone = milestoneService.createPackageDimensionUpdateMilestone(updatedShipment);
        updatedShipment.setMilestone(milestone);
        updatedShipment.setMilestoneEvents(List.of(milestone));
        messageApi.sendMilestoneMessage(updatedShipment, TriggeredFrom.SHP);
    }

}
