package com.quincus.shipment.impl.repository.entity.component;

import com.quincus.shipment.impl.repository.listeners.BaseEntityListener;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import java.time.Instant;

@MappedSuperclass
@EqualsAndHashCode
@Data
@EntityListeners(BaseEntityListener.class)
public class BaseEntity implements IdentifiableEntity {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    protected String id;
    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    protected Instant createTime;
    @UpdateTimestamp
    @Column(name = "modify_time")
    protected Instant modifyTime;
    @Version
    @Column(name = "version")
    protected Long version;
}
