package com.quincus.shipment.impl.attachment;

import com.quincus.shipment.api.constant.AttachmentType;
import com.quincus.shipment.impl.attachment.milestone.MilestoneAttachmentService;
import com.quincus.shipment.impl.attachment.networklane.NetworkLaneAttachmentService;
import com.quincus.shipment.impl.attachment.packagejourneyairsegment.PackageJourneyAirSegmentAttachmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceFactoryTest {

    @Mock
    private PackageJourneyAirSegmentAttachmentService packageJourneyAirSegmentAttachmentService;
    @Mock
    private MilestoneAttachmentService milestoneAttachmentService;
    @Mock
    private NetworkLaneAttachmentService networkLaneAttachmentService;
    
    private AttachmentServiceFactory attachmentServiceFactory;

    @BeforeEach
    public void setUp() {
        List<AbstractAttachmentService<?>> attachmentServiceList = createAttachmentServiceLists();
        when(packageJourneyAirSegmentAttachmentService.getAttachmentType()).thenReturn(AttachmentType.PACKAGE_JOURNEY_AIR_SEGMENT);
        when(milestoneAttachmentService.getAttachmentType()).thenReturn(AttachmentType.MILESTONE);
        when(networkLaneAttachmentService.getAttachmentType()).thenReturn(AttachmentType.NETWORK_LANE);
        attachmentServiceFactory = new AttachmentServiceFactory(attachmentServiceList);
    }

    private List<AbstractAttachmentService<?>> createAttachmentServiceLists() {
        List<AbstractAttachmentService<?>> attachmentServiceList = new ArrayList<>();
        attachmentServiceList.add(packageJourneyAirSegmentAttachmentService);
        attachmentServiceList.add(milestoneAttachmentService);
        attachmentServiceList.add(networkLaneAttachmentService);
        return attachmentServiceList;
    }

    @DisplayName("Given attachment type When GetAttachmentService Then get expected AttachmentService")
    @ParameterizedTest
    @MethodSource("attachmentTypeProvider")
    void givenAttachmentTypeWhenGetAttachmentServiceThenReturnExpectedService(AttachmentType attachmentType,
                                                                              Class<? extends AbstractAttachmentService<?>> expectedServiceClass) {
        AbstractAttachmentService<?> attachmentService = attachmentServiceFactory.getAttachmentServiceByType(attachmentType);

        assertThat(attachmentService).isInstanceOf(expectedServiceClass);
    }

    private static Stream<Arguments> attachmentTypeProvider() {
        return Stream.of(
                Arguments.of(AttachmentType.PACKAGE_JOURNEY_AIR_SEGMENT, PackageJourneyAirSegmentAttachmentService.class),
                Arguments.of(AttachmentType.MILESTONE, MilestoneAttachmentService.class),
                Arguments.of(AttachmentType.NETWORK_LANE, NetworkLaneAttachmentService.class)
        );
    }
}
