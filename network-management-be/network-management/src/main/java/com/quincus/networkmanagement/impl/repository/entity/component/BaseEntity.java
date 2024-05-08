package com.quincus.networkmanagement.impl.repository.entity.component;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import java.time.Instant;

@MappedSuperclass
@EqualsAndHashCode
@Data
public class BaseEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
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
