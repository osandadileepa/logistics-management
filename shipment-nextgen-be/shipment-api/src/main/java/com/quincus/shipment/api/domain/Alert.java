package com.quincus.shipment.api.domain;

import com.quincus.shipment.api.constant.AlertLevel;
import com.quincus.shipment.api.constant.AlertMessage;
import com.quincus.shipment.api.constant.AlertType;
import com.quincus.shipment.api.constant.ConstraintType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class Alert implements Comparable<Alert> {
    private String id;
    @Size(max = 128)
    private String shortMessage;

    @NotBlank
    @Size(max = 256)
    private String message;
    @NotNull
    private AlertType type;
    @NotNull
    private ConstraintType constraint;
    private AlertLevel level;
    private boolean dismissed;
    private LocalDateTime dismissTime;
    private String dismissedBy;
    private String shipmentJourneyId;
    private String packageJourneySegmentId;
    private List<String> fields;

    public Alert(String shortMessage, AlertType type) {
        this.shortMessage = shortMessage;
        this.type = type;
        this.constraint = getConstraint();
    }

    public Alert(AlertMessage alertMessage, AlertType type) {
        this.shortMessage = alertMessage.toString();
        this.message = alertMessage.getFullMessage();
        this.type = type;
        this.level = alertMessage.getLevel();
        this.constraint = alertMessage.getConstraintType();
    }

    public Alert(AlertMessage alertMessage, List<String> alertMessageDetails, AlertType type) {
        this.shortMessage = alertMessage.toString();
        this.message = String.format("%s %s", alertMessage.getFullMessage(), String.join("", alertMessageDetails));
        this.type = type;
        this.level = alertMessage.getLevel();
        this.constraint = alertMessage.getConstraintType();
    }

    public Alert(AlertMessage alertMessage, AlertType type, List<String> fields) {
        this(alertMessage, type);
        this.fields = fields;
    }

    public Alert(AlertMessage alertMessage, List<String> alertMessageDetails, AlertType type, List<String> fields) {
        this(alertMessage, alertMessageDetails, type);
        this.fields = fields;
    }

    public ConstraintType getConstraint() {
        if (this.type == AlertType.JOURNEY_REVIEW_REQUIRED) {
            return this.constraint != null ? this.constraint : ConstraintType.SOFT_CONSTRAINT;
        } else if (this.type == AlertType.ERROR) {
            return ConstraintType.HARD_CONSTRAINT;
        } else if (this.type == AlertType.WARNING) {
            return ConstraintType.SOFT_CONSTRAINT;
        }
        return null;
    }

    @Override
    public int compareTo(Alert alert) {
        return Comparator.comparing(Alert::getType)
                .thenComparing(Alert::getConstraint)
                .compare(this, alert);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Alert that)) {
            return false;
        }
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
