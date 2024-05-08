package com.quincus.shipment.impl.repository.entity;

import com.quincus.shipment.api.constant.AlertLevel;
import com.quincus.shipment.api.constant.AlertType;
import com.quincus.shipment.api.constant.ConstraintType;
import com.quincus.shipment.impl.repository.entity.component.BaseEntity;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@TypeDef(name = "json", typeClass = JsonStringType.class)
@Table(name = "alert")
@FilterDef(name = "dismissedFilter", defaultCondition = "isDismissed = false")
@Filter(name = "dismissedFilter")
@Where(clause = "dismissed=false")
public class AlertEntity extends BaseEntity {

    @Column(name = "short_message")
    private String shortMessage;

    @Column(name = "message")
    private String message;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private AlertType type;

    @Column(name = "constraint_type")
    @Enumerated(EnumType.STRING)
    private ConstraintType constraint;

    @Column(name = "level")
    @Enumerated(EnumType.STRING)
    private AlertLevel level;

    @Column(name = "dismissed")
    private boolean dismissed;

    @Column(name = "dismiss_time")
    private LocalDateTime dismissTime;

    @Column(name = "dismissed_by")
    private String dismissedBy;

    @Column(name = "fields")
    @Type(type = "json")
    private List<String> fields;

    @Column(name = "shipment_journey_id")
    private String shipmentJourneyId;

    @Column(name = "package_journey_segment_id")
    private String packageJourneySegmentId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        AlertEntity that = (AlertEntity) o;
        return (message != null && Objects.equals(message, that.message)) &&
                Objects.equals(shipmentJourneyId, that.shipmentJourneyId) &&
                Objects.equals(packageJourneySegmentId, that.packageJourneySegmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, shipmentJourneyId, packageJourneySegmentId);
    }
}
