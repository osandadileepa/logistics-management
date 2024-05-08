package com.quincus.shipment.impl.repository;

import com.quincus.shipment.impl.repository.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.Tuple;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, String> {

    @Query(value = """
            SELECT DISTINCT
              order1.id as id, order1.status as status
            FROM
              ShipmentEntity shp LEFT JOIN shp.order order1
            WHERE
              shp.id in (:shipmentIds)
            """)
    List<Tuple> findIdAndStatusByShipmentIds(List<String> shipmentIds);
}
