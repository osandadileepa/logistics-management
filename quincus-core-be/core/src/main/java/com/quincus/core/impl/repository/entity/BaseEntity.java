package com.quincus.core.impl.repository.entity;

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
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @EqualsAndHashCode.Include
    @ToString.Include
    private String id;
    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private Instant createTime;
    @UpdateTimestamp
    @Column(name = "modify_time")
    private Instant modifyTime;
    @Version
    @Column(name = "version")
    private Long version;
}
