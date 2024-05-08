package com.quincus.shipment.impl.test_utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.quincus.order.api.domain.Root;
import com.quincus.order.api.domain.SegmentsPayload;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.impl.config.ShipmentJourneyCreationProperties;
import com.quincus.shipment.impl.converter.ShipmentOrderMessageConverter;
import com.quincus.shipment.impl.helper.journey.PackageJourneySegmentTypeAssigner;
import com.quincus.shipment.impl.helper.journey.ShipmentJourneyProvider;
import com.quincus.shipment.impl.helper.journey.generator.OrderMessageShipmentJourneyGenerator;
import com.quincus.shipment.impl.helper.journey.generator.ShipmentJourneyGenerator;
import com.quincus.shipment.impl.mapper.MilestoneMapper;
import com.quincus.shipment.impl.repository.entity.component.BaseEntity;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.shipment.impl.validator.RootOrderValidator;
import com.quincus.web.common.multitenant.UserDetailsContextHolder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.factory.Mappers;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import javax.persistence.EntityManager;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static org.assertj.core.api.Fail.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@Slf4j
public class TestUtil {

    private static final TestUtil INSTANCE = new TestUtil();
    private static final String SEGMENTS_PAYLOAD = "segments_payload";
    private static final Pattern INPUT_DATE_TIME_STR_PATTERN = Pattern.compile(
            "(\\d{4})-(\\d{2})-(\\d{2})\\s?T?(\\d{2}):(\\d{2}):(\\d{2}).*");
    @Getter
    private final ObjectMapper objectMapper;
    @Getter
    private final EntityManager entityManager;
    @Getter
    private final MilestoneMapper milestoneMapper;

    private TestUtil() {
        final Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        builder.modules(new JavaTimeModule());
        final ObjectMapper objectMapper = builder.build();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.objectMapper = objectMapper;
        this.entityManager = mock(EntityManager.class);
        lenient().when(entityManager.getReference(any(Class.class), any())).thenAnswer(i -> {
            Class<? extends BaseEntity> clazz = i.getArgument(0);
            String id = i.getArgument(1);
            BaseEntity entity;
            entity = clazz.getDeclaredConstructor().newInstance();
            entity.setId(id);
            return entity;
        });
        this.milestoneMapper = Mappers.getMapper(MilestoneMapper.class);
        this.milestoneMapper.setObjectMapper(objectMapper);
        this.milestoneMapper.setEntityManager(entityManager);
    }

    public static TestUtil getInstance() {
        return INSTANCE;
    }

    public File getFile(String filePath) throws IOException {
        return new ClassPathResource(filePath).getFile();
    }

    public Shipment createSingleShipmentData() {
        JsonNode data = getDataFromFile("samplepayload/request/createSingleShipment.json");
        return this.objectMapper.convertValue(data.get("data"), Shipment.class);
    }

    public Shipment[] createBulkShipmentData() {
        JsonNode data = getDataFromFile("samplepayload/request/createBulkShipment.json");
        return this.objectMapper.convertValue(data.get("data"), Shipment[].class);
    }

    public List<Shipment> createShipmentsFromOrder(String jsonPath) {
        JsonNode data = getDataFromFile(jsonPath);
        ObjectNode dataObj = (ObjectNode) data;
        dataObj.put("distance_uom", Root.DISTANCE_UOM_METRIC);

        UserDetailsProvider userDetailsProvider = createDummyUserDetailsProvider("orgId1", "partner1");

        int segmentSize = (dataObj.get("segments_payload") != null) ? dataObj.get("segments_payload").size() : 1;
        List<ShipmentJourneyGenerator> journeyGeneratorList = IntStream.range(0, segmentSize)
                .mapToObj(s -> (ShipmentJourneyGenerator) new OrderMessageShipmentJourneyGenerator(new PackageJourneySegmentTypeAssigner(), createDummyJourneyCreationProperties(), userDetailsProvider))
                .toList();

        ShipmentJourneyProvider journeyProvider = new ShipmentJourneyProvider(journeyGeneratorList);

        ShipmentOrderMessageConverter convert = new ShipmentOrderMessageConverter(objectMapper, userDetailsProvider, journeyProvider, mock(RootOrderValidator.class));
        return convert.convertOrderMessageToShipments(data.toString(), "");
    }

    public String invalidShipmentJson() throws JsonProcessingException {
        JsonNode data = getDataFromFile("samplepayload/request/createSingleInvalidShipment.json");
        return this.objectMapper.writeValueAsString(data);
    }

    public String validShipmentJson() throws JsonProcessingException {
        JsonNode data = getDataFromFile("samplepayload/request/createSingleShipment.json");
        return this.objectMapper.writeValueAsString(data);
    }

    public String validCostJson() throws JsonProcessingException {
        JsonNode data = getDataFromFile("samplepayload/request/costRequest.json");
        return this.objectMapper.writeValueAsString(data);
    }

    public String validUpdateShipmentsPackageDimensionRQJson() throws JsonProcessingException {
        JsonNode data = getDataFromFile("samplepayload/request/updateShipmentsPackageDimensionRequest.json");
        return this.objectMapper.writeValueAsString(data);
    }

    public JsonNode getDataFromFile(String jsonPath) {
        try {
            ClassPathResource path = new ClassPathResource(jsonPath);
            return this.objectMapper.readValue(path.getFile(), JsonNode.class);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return this.objectMapper.createObjectNode();
    }

    public boolean isDateTimeFromString(String expectedDateTimeStr, LocalDateTime localDateTime) {
        if (expectedDateTimeStr == null) {
            return (localDateTime == null);
        }

        var expectedDateTimeStrMatcher = INPUT_DATE_TIME_STR_PATTERN.matcher(expectedDateTimeStr);
        if (!expectedDateTimeStrMatcher.matches()) {
            fail("Expected Date Time String format is not defined for the test case.");
        }

        var expectedYear = Integer.parseInt(expectedDateTimeStrMatcher.group(1));
        var expectedMonth = Integer.parseInt(expectedDateTimeStrMatcher.group(2));
        var expectedDay = Integer.parseInt(expectedDateTimeStrMatcher.group(3));
        var expectedHour = Integer.parseInt(expectedDateTimeStrMatcher.group(4));
        var expectedMinute = Integer.parseInt(expectedDateTimeStrMatcher.group(5));
        var expectedSecond = Integer.parseInt(expectedDateTimeStrMatcher.group(6));

        var year = localDateTime.getYear();
        var month = localDateTime.getMonthValue();
        var day = localDateTime.getDayOfMonth();
        var hour = localDateTime.getHour();
        var minute = localDateTime.getMinute();
        var second = localDateTime.getSecond();

        return (expectedYear == year)
                && (expectedMonth == month)
                && (expectedDay == day)
                && (expectedHour == hour)
                && (expectedMinute == minute)
                && (expectedSecond == second);
    }

    public Root createRootFromOM(String msg) throws JsonProcessingException {
        return getObjectMapper().readValue(msg, Root.class);
    }

    public List<SegmentsPayload> createSegmentListFromOM(String msg, String isSegment) throws JsonProcessingException {
        List<SegmentsPayload> segList = new ArrayList<>();
        if (Boolean.TRUE.equals(Boolean.valueOf(isSegment))) {
            JsonNode json = objectMapper.readValue(msg, JsonNode.class);
            JsonNode seg = json.get(SEGMENTS_PAYLOAD);
            List<JsonNode> list = objectMapper.convertValue(seg, new TypeReference<>() {
            });
            int size = list.size();
            IntStream.range(0, size)
                    .forEach(index -> {
                        JsonNode omSegment = list.get(index);
                        SegmentsPayload segment = objectMapper.convertValue(omSegment, SegmentsPayload.class);
                        String sequence = String.valueOf(index);
                        segment.setSequence(sequence);
                        if (StringUtils.isBlank(segment.getRefId())) {
                            segment.setRefId(sequence);
                        }
                        segList.add(segment);
                    });
        }
        return segList;
    }

    public UserDetailsProvider createDummyUserDetailsProvider(String orgId, String partnerId) {
        UserDetailsContextHolder userDetailsContextHolder = mock(UserDetailsContextHolder.class);

        lenient().when(userDetailsContextHolder.getCurrentOrganizationId()).thenReturn(orgId);
        lenient().when(userDetailsContextHolder.getCurrentPartnerId()).thenReturn(partnerId);

        return new UserDetailsProvider(userDetailsContextHolder);
    }

    public ShipmentJourneyCreationProperties createDummyJourneyCreationProperties() {
        ShipmentJourneyCreationProperties shipmentJourneyCreationProperties = mock(ShipmentJourneyCreationProperties.class);
        lenient().when(shipmentJourneyCreationProperties.getOrganizationIdsToSkipSegmentCreationFromPayload()).thenReturn(Collections.emptyList());
        return shipmentJourneyCreationProperties;
    }
}
