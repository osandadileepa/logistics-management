package com.quincus.shipment.impl.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quincus.order.api.domain.Root;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.impl.config.ShipmentJourneyCreationProperties;
import com.quincus.shipment.impl.helper.journey.PackageJourneySegmentTypeAssigner;
import com.quincus.shipment.impl.helper.journey.ShipmentJourneyProvider;
import com.quincus.shipment.impl.helper.journey.generator.OrderMessageShipmentJourneyGenerator;
import com.quincus.shipment.impl.helper.journey.generator.ShipmentJourneyGenerator;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.shipment.impl.test_utils.TestUtil;
import com.quincus.shipment.impl.validator.RootOrderValidator;
import com.quincus.web.common.exception.model.QuincusValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.IntStream;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentOrderMessageConverterTest {
    private final TestUtil testUtil = TestUtil.getInstance();
    @Mock
    private UserDetailsProvider userDetailsProvider;
    @Mock
    private ShipmentJourneyCreationProperties shipmentJourneyCreationProperties;
    @Mock
    private ShipmentJourneyProvider shipmentJourneyProvider;
    @Mock
    private RootOrderValidator rootOrderValidator;
    private ShipmentOrderMessageConverter shipmentOrderMessageConverter;

    @BeforeEach
    public void setUp() {
        shipmentOrderMessageConverter = new ShipmentOrderMessageConverter(testUtil.getObjectMapper(), userDetailsProvider, shipmentJourneyProvider, rootOrderValidator);
    }

    @Test
    void convert_withValidMessage_shouldReturnShipmentList() throws JsonProcessingException {
        JsonNode data = testUtil.getDataFromFile("samplepayload/ordermodule-orders.json");
        Root root = testUtil.createRootFromOM(data.toString());

        when(shipmentJourneyProvider.generateShipmentJourney(any())).thenReturn(mock(ShipmentJourney.class));

        List<Shipment> result = convertOrderMessageToShipments(data);
        assertThat(root.getSegmentsPayloads()).isNull();
        assertThat(result).isNotNull();
        assertThat(root.getShipments().get(0).getPackageAddon()).isNotNull();
        assertThat(root.getShipments().get(0).getPackageInsurance()).isNotNull();

        Shipment shipmentResult = result.get(0);
        assertThat(shipmentResult.getOrderId()).isEqualTo(shipmentResult.getOrder().getId());
        assertThat(shipmentResult.getShipmentJourney()).isNotNull();
        assertThat(shipmentResult.getUserId())
                .isEqualTo(root.getUserId());
        assertThat(shipmentResult.getOrder().getId())
                .isEqualTo(root.getId());
        assertThat(shipmentResult.getShipmentTrackingId())
                .isEqualTo(root.getShipments().get(0).getShipmentIdLabel());
        assertThat(shipmentResult.getNotes())
                .isEqualTo(root.getShipments().get(0).getNote());
        assertThat(shipmentResult.getShipmentPackage().getDimension().getLength().doubleValue())
                .isEqualTo(root.getShipments().get(0).getLength());
        assertThat(shipmentResult.getShipmentPackage().getDimension().getHeight().doubleValue())
                .isEqualTo(root.getShipments().get(0).getHeight());
        assertThat(shipmentResult.getShipmentPackage().getDimension().getWidth().doubleValue())
                .isEqualTo(root.getShipments().get(0).getWidth());
        assertThat(shipmentResult.getShipmentPackage().getDimension().getVolumeWeight().doubleValue())
                .isEqualTo(root.getShipments().get(0).getVolumeWeight());
        assertThat(shipmentResult.getShipmentPackage().getDimension().getGrossWeight().doubleValue())
                .isEqualTo(root.getShipments().get(0).getGrossWeight());
        assertThat(shipmentResult.getShipmentPackage().getDimension().getChargeableWeight().doubleValue())
                .isEqualTo(root.getShipments().get(0).getChargeableWeight());
        assertThat(shipmentResult.getInstructions()).isNotNull();
        assertThat(shipmentResult.getInstructions()).isEqualTo(shipmentResult.getOrder().getInstructions());
    }

    @Test
    void convert_withSegment_shouldReturnShipmentList() {
        JsonNode data = testUtil.getDataFromFile("samplepayload/ordermodule-orders-with-segment.json");
        ObjectNode dataObj = (ObjectNode) data;
        setupWithShipmentJourneyGenerators(dataObj);

        List<Shipment> result = convertOrderMessageToShipments(data);

        Shipment shipmentResult = result.get(0);
        assertThat(shipmentResult.getShipmentJourney().getPackageJourneySegments())
                .hasSize(5);
        Order order = shipmentResult.getOrder();
        assertThat(shipmentResult.getOrderId()).isEqualTo(order.getId());
        assertThat(order).isNotNull();
        assertThat(shipmentResult.getInstructions()).isEqualTo(order.getInstructions());
        verify(rootOrderValidator, times(1)).validate(any());
    }

    @Test
    void givenInvalidOrderJsonWhenConvertShouldThrowQuincusValidationException() {
        JsonNode data = testUtil.getDataFromFile("samplepayload/ordermodule-orders-with-invalid-package-addon-and-insurance.json");

        assertThatThrownBy(() -> convertOrderMessageToShipments(data)).isInstanceOf(QuincusValidationException.class);
    }

    private List<Shipment> convertOrderMessageToShipments(JsonNode data) {
        return shipmentOrderMessageConverter.convertOrderMessageToShipments(data.toString(), randomUUID().toString());
    }

    private void setupWithShipmentJourneyGenerators(ObjectNode dataObj) {
        int segmentSize = (dataObj.get("segments_payload") != null) ? dataObj.get("segments_payload").size() : 1;
        List<ShipmentJourneyGenerator> journeyGeneratorList = IntStream.range(0, segmentSize)
                .mapToObj(s -> (ShipmentJourneyGenerator) new OrderMessageShipmentJourneyGenerator(
                        new PackageJourneySegmentTypeAssigner(), shipmentJourneyCreationProperties, userDetailsProvider))
                .toList();
        shipmentJourneyProvider = new ShipmentJourneyProvider(journeyGeneratorList);
        shipmentOrderMessageConverter = new ShipmentOrderMessageConverter(testUtil.getObjectMapper(), userDetailsProvider, shipmentJourneyProvider, rootOrderValidator);
    }
}
