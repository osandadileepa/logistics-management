package com.quincus.shipment.impl.service;

import com.quincus.order.api.domain.Root;
import com.quincus.shipment.impl.repository.OrderRepository;
import com.quincus.shipment.impl.repository.entity.OrderEntity;
import com.quincus.shipment.impl.test_utils.TupleDataFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.Tuple;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Test
    void findById_orderFound_shouldReturnOrderEntity() {
        String orderId = "ORDER1";

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(new OrderEntity()));

        OrderEntity resultOrder = orderService.findById(orderId);

        assertThat(resultOrder).isNotNull();

        verify(orderRepository, times(1)).findById(anyString());
    }

    @Test
    void findById_orderNotFound_shouldReturnNull() {
        String orderId = "ORDER1";

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        OrderEntity resultOrder = orderService.findById(orderId);

        assertThat(resultOrder).isNull();

        verify(orderRepository, times(1)).findById(anyString());
    }

    @Test
    void findById_nullArguments_shouldReturnNull() {
        OrderEntity resultOrder = orderService.findById(null);

        assertThat(resultOrder).isNull();

        verify(orderRepository, never()).findById(anyString());
    }

    @Test
    void findOrCreateOrder_orderFound_shouldReturnExistingOrder() {
        String orderId = "ORDER1";
        OrderEntity order = new OrderEntity();
        order.setId(orderId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        OrderEntity resultOrder = orderService.findOrCreateOrder(order);

        assertThat(resultOrder).isNotNull();

        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void findOrCreateOrder_orderNotFound_shouldReturnNewOrder() {
        String orderId = "ORDER1";
        OrderEntity order = new OrderEntity();
        order.setId(orderId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        OrderEntity resultOrder = orderService.findOrCreateOrder(order);

        assertThat(resultOrder).isNotNull().isEqualTo(order);

        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).save(any());
    }

    @Test
    void findOrCreateOrder_nullArguments_shouldReturnNull() {
        OrderEntity resultOrder = orderService.findOrCreateOrder(null);

        assertThat(resultOrder).isNull();

        verify(orderRepository, never()).findById(anyString());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void findOrCreateOrder_argumentNullId_shouldReturnNull() {
        OrderEntity order = new OrderEntity();

        OrderEntity resultOrder = orderService.findOrCreateOrder(order);

        assertThat(resultOrder).isNull();

        verify(orderRepository, never()).findById(anyString());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void findOrCreateOrder_saveThrowsException_shouldReturnExistingOrder() {
        String orderId = "ORDER1";
        OrderEntity order = new OrderEntity();
        order.setId(orderId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty())
                .thenReturn(Optional.of(order));
        doThrow(new ConstraintViolationException("Not an exception. Test Only.", new SQLException(), "none"))
                .when(orderRepository).save(any(OrderEntity.class));

        OrderEntity resultOrder = orderService.findOrCreateOrder(order);

        assertThat(resultOrder).isNotNull();

        verify(orderRepository, times(2)).findById(orderId);
        verify(orderRepository, times(1)).save(any());
    }

    @Test
    void findStatusByShipmentIds_orderTuplesFound_shouldReturnTupleOfIdAndStatus() {
        OrderEntity orderEntity1 = new OrderEntity();
        orderEntity1.setId("order-1");
        orderEntity1.setStatus("PLANNED");
        OrderEntity orderEntity2 = new OrderEntity();
        orderEntity2.setId("order-2");
        orderEntity2.setStatus(Root.STATUS_CREATED);
        List<OrderEntity> orderEntities = List.of(orderEntity1, orderEntity2);
        List<Tuple> orderTuple = orderEntities.stream()
                .map(o -> TupleDataFactory.ofOrderWithIdAndStatus(o.getId(), o.getStatus())).toList();
        when(orderRepository.findIdAndStatusByShipmentIds(anyList())).thenReturn(orderTuple);

        List<OrderEntity> result = orderService.findStatusByShipmentIds(List.of("a", "b"));
        assertThat(result).hasSize(orderEntities.size());
        assertThat(result.get(0).getId()).isEqualTo(orderEntities.get(0).getId());
        assertThat(result.get(0).getStatus()).isEqualTo(orderEntities.get(0).getStatus());
        assertThat(result.get(1).getId()).isEqualTo(orderEntities.get(1).getId());
        assertThat(result.get(1).getStatus()).isEqualTo(orderEntities.get(1).getStatus());
    }

    @Test
    void findStatusByShipmentIds_noResults_shouldReturnTupleOfIdAndStatus() {
        when(orderRepository.findIdAndStatusByShipmentIds(anyList())).thenReturn(Collections.emptyList());

        List<OrderEntity> result = orderService.findStatusByShipmentIds(List.of("a", "b"));
        assertThat(result).isEmpty();
    }

    @Test
    void givenOrderEntityIsNull_whenCreatingOrder_thenReturnNull() {
        OrderEntity resultOrder = orderService.createOrUpdateOrder(null);

        assertThat(resultOrder).isNull();

        verify(orderRepository, never()).save(any());
    }

    @Test
    void givenOrderEntityIsValid_whenCreatingOrder_thenReturnSavedOrderEntity() {
        OrderEntity givenOrder = new OrderEntity();
        givenOrder.setStatus("SampleStatus");

        OrderEntity givenSavedOrder = new OrderEntity();
        givenSavedOrder.setId(UUID.randomUUID().toString());
        givenSavedOrder.setStatus("SampleStatus");
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(givenSavedOrder);

        OrderEntity resultOrder = orderService.createOrUpdateOrder(givenOrder);

        assertThat(resultOrder).isNotNull();
        verify(orderRepository, times(1)).save(any());
        assertThat(resultOrder.getId()).isEqualTo(givenSavedOrder.getId());
    }

}
