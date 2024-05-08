package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.impl.repository.OrderRepository;
import com.quincus.shipment.impl.repository.entity.OrderEntity;
import com.quincus.shipment.impl.repository.entity.OrderEntity_;
import com.quincus.shipment.impl.repository.entity.component.BaseEntity_;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.persistence.Tuple;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class OrderService {
    private final OrderRepository orderRepository;

    public OrderEntity findById(final String id) {
        if (id == null) {
            return null;
        }
        return orderRepository.findById(id).orElse(null);
    }

    @Transactional
    public OrderEntity findOrCreateOrder(final OrderEntity orderEntity) {
        if ((orderEntity == null) || (orderEntity.getId() == null)) {
            return null;
        }

        final OrderEntity existingOrderEntity = orderRepository.findById(orderEntity.getId()).orElse(null);
        try {
            return existingOrderEntity == null ?
                    orderRepository.save(orderEntity) : existingOrderEntity;
        } catch (ConstraintViolationException constraintViolationException) {
            return orderRepository.findById(orderEntity.getId()).orElse(null);
        }
    }

    @Transactional
    public OrderEntity createOrUpdateOrder(final OrderEntity orderEntity) {
        if (orderEntity == null) {
            return null;
        }
        return orderRepository.save(orderEntity);
    }

    public List<OrderEntity> findStatusByShipmentIds(List<String> shipmentIds) {
        List<Tuple> orderTuples = orderRepository.findIdAndStatusByShipmentIds(shipmentIds);
        if (CollectionUtils.isEmpty(orderTuples)) {
            return Collections.emptyList();
        }
        return orderTuples.stream().map(this::convertIdAndStatusTupleToEntity)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Transactional
    public void updateStatus(Order orderDomain) {
        final OrderEntity order = orderRepository.findById(orderDomain.getId()).orElse(null);
        if (nonNull(order) && !StringUtils.equalsIgnoreCase(orderDomain.getStatus(), order.getStatus())) {
            order.setStatus(orderDomain.getStatus());
            orderRepository.save(order);
        }
    }

    private OrderEntity convertIdAndStatusTupleToEntity(Tuple orderTuple) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(orderTuple.get(BaseEntity_.ID, String.class));
        orderEntity.setStatus(orderTuple.get(OrderEntity_.STATUS, String.class));
        return orderEntity;
    }
}
