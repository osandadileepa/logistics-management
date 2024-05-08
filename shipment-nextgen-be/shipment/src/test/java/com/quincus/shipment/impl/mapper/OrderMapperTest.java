package com.quincus.shipment.impl.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.order.api.domain.Root;
import com.quincus.shipment.api.domain.Instruction;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.impl.repository.entity.InstructionEntity;
import com.quincus.shipment.impl.repository.entity.OrderEntity;
import com.quincus.shipment.impl.test_utils.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OrderMapperTest {

    private final TestUtil testUtil = TestUtil.getInstance();
    private final ObjectMapper objectMapper = testUtil.getObjectMapper();

    @Test
    void mapDomainToEntity_orderDomain_shouldReturnOrderEntity() {
        Order domain = new Order();
        domain.setId("ORDER-1");
        domain.setGroup("GRP1");
        domain.setOpsType("Hub");
        domain.setData("OM-PAYLOAD");
        domain.setTrackingUrl("tracking-url");
        domain.setOrderIdLabel("LABEL1");
        domain.setCustomerReferenceId(List.of("REF1", "ID2"));
        domain.setPickupStartTime(LocalDateTime.now().toString());
        domain.setPickupCommitTime(LocalDateTime.now().plusHours(1).toString());
        domain.setPickupTimezone("GMT+8:00");
        domain.setDeliveryStartTime(LocalDateTime.now().plusDays(1).toString());
        domain.setDeliveryCommitTime(LocalDateTime.now().plusDays(1).plusHours(1).toString());
        domain.setDeliveryTimezone("GMT-7:00");
        domain.setStatus(Root.STATUS_CREATED);

        Instruction instruction1 = new Instruction();
        instruction1.setId("10001");
        instruction1.setValue("instruction 1");
        Instruction instruction2 = new Instruction();
        instruction2.setId("10002");
        instruction2.setValue("instruction 2");
        domain.setInstructions(List.of(instruction1, instruction2));

        final OrderEntity entity = OrderMapper.mapDomainToEntity(domain, objectMapper);

        assertThat(entity)
                .usingRecursiveComparison()
                .ignoringFields("customerReferenceId", "data", "tags", "attachments", "instructions", "version", "modifyTime", "createTime")
                .isEqualTo(domain);

        List<String> domainOrderCustomerRefIdList = domain.getCustomerReferenceId();
        List<String> entityOrderCustomerRefIdList = entity.getCustomerReferenceId();
        assertThat(entityOrderCustomerRefIdList).isEqualTo(domainOrderCustomerRefIdList);
        assertThat(entity.getInstructions().get(0).getValue()).isEqualTo(instruction1.getValue());
        assertThat(entity.getInstructions().get(1).getValue()).isEqualTo(instruction2.getValue());
        assertThat(entity.getData().asText()).isEqualTo(domain.getData());
    }

    @Test
    void mapDomainToEntity_orderDomainNull_shouldReturnNull() {
        assertThat(OrderMapper.mapDomainToEntity(null, objectMapper)).isNull();
    }

    @Test
    void mapEntityToDomain_orderEntity_shouldReturnOrderDomain() {
        OrderEntity entity = new OrderEntity();
        entity.setId("id1");
        entity.setGroup("group1");
        entity.setOpsType("Hub");
        entity.setData(objectMapper.convertValue("om-payload", JsonNode.class));
        List<String> entityOrderCustomerRefIdList = Arrays.asList("ref1", "id2");
        entity.setCustomerReferenceId(entityOrderCustomerRefIdList);
        entity.setOrderIdLabel("label-1");
        entity.setTrackingUrl("tracking-url");
        entity.setPickupStartTime(LocalDateTime.now().toString());
        entity.setPickupCommitTime(LocalDateTime.now().plusHours(1).toString());
        entity.setPickupTimezone("GMT+8:00");
        entity.setDeliveryStartTime(LocalDateTime.now().plusDays(1).toString());
        entity.setDeliveryCommitTime(LocalDateTime.now().plusDays(1).plusHours(1).toString());
        entity.setDeliveryTimezone("GMT-7:00");
        entity.setStatus(Root.STATUS_CREATED);

        InstructionEntity instructionEntity1 = new InstructionEntity();
        instructionEntity1.setId("1001");
        instructionEntity1.setValue("INSTRUCTION1");
        InstructionEntity instructionEntity2 = new InstructionEntity();
        instructionEntity2.setId("1002");
        instructionEntity2.setValue("INSTRUCTION2");
        entity.setInstructions(List.of(instructionEntity1, instructionEntity2));

        final Order domain = OrderMapper.mapEntityToDomain(entity);

        assertThat(domain)
                .usingRecursiveComparison()
                .ignoringFields("customerReferenceId", "data", "tags", "attachments", "segmentsUpdated", "usedOpenApi", "code", "timeCreated")
                .isEqualTo(entity);

        List<String> domainOrderCustomerRefIdList = domain.getCustomerReferenceId();
        assertThat(domainOrderCustomerRefIdList).isEqualTo(entityOrderCustomerRefIdList);
        assertThat(domain.getInstructions().get(0).getValue()).isEqualTo(instructionEntity1.getValue());
        assertThat(domain.getInstructions().get(1).getValue()).isEqualTo(instructionEntity2.getValue());
        assertThat(domain.getData()).isEqualTo(entity.getData().asText());
    }

    @Test
    void mapEntityToDomain_orderEntityNull_shouldReturnNull() {
        assertThat(OrderMapper.mapEntityToDomain(null)).isNull();
    }

    @Test
    void toOrderForShipmentJourneyUpdate_orderEntity_shouldReturnOrderDomain() {
        OrderEntity entity = new OrderEntity();
        entity.setId("id1");
        entity.setGroup("group1");
        entity.setOpsType("Hub");
        entity.setData(objectMapper.convertValue("om-payload", JsonNode.class));
        List<String> entityOrderCustomerRefIdList = Arrays.asList("ref1", "id2");
        entity.setCustomerReferenceId(entityOrderCustomerRefIdList);
        entity.setOrderIdLabel("label-1");
        entity.setTrackingUrl("tracking-url");
        entity.setPickupStartTime(LocalDateTime.now().toString());
        entity.setPickupCommitTime(LocalDateTime.now().plusHours(1).toString());
        entity.setPickupTimezone("GMT+8:00");
        entity.setDeliveryStartTime(LocalDateTime.now().plusDays(1).toString());
        entity.setDeliveryCommitTime(LocalDateTime.now().plusDays(1).plusHours(1).toString());
        entity.setDeliveryTimezone("GMT-7:00");
        entity.setStatus(Root.STATUS_CREATED);

        final Order domain = OrderMapper.toOrderForShipmentJourneyUpdate(entity);

        assertThat(domain)
                .usingRecursiveComparison()
                .ignoringFields("customerReferenceId", "data", "tags", "attachments", "instructions", "segmentsUpdated", "usedOpenApi", "code", "timeCreated")
                .isEqualTo(entity);

        List<String> domainOrderCustomerRefIdList = domain.getCustomerReferenceId();
        assertThat(domainOrderCustomerRefIdList).isEqualTo(entityOrderCustomerRefIdList);
        assertThat(domain.getData()).isEqualTo(entity.getData().asText());
    }
}
