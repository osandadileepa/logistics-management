package com.quincus.shipment.api.domain;

import com.quincus.shipment.api.constant.AlertType;
import com.quincus.shipment.api.constant.ConstraintType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AlertValidationTest extends ValidationTest {

    @Test
    @DisplayName("Given blank message when validate alert then fail")
    void failWhenAlertMessageIsBlank() {
        Alert alert = new Alert();
        alert.setShortMessage("");
        assertThat(validateModel(alert)).isNotEmpty();
    }

    @Test
    @DisplayName("Given mandatory fields when validate alert then pass")
    void passWhenMandatoryFieldsAreProvided() {
        Alert alert = new Alert();
        alert.setShortMessage("This is a warning message");
        alert.setMessage("This is a warning message [XXX]");
        alert.setType(AlertType.WARNING);
        alert.setConstraint(ConstraintType.SOFT_CONSTRAINT);
        assertThat(validateModel(alert)).isEmpty();
    }

}
