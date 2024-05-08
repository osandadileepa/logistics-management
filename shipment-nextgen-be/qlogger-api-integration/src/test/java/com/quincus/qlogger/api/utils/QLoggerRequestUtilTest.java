package com.quincus.qlogger.api.utils;

import com.quincus.qlogger.api.QLoggerCategory;
import com.quincus.qlogger.model.QLoggerRequest;
import com.quincus.shipment.api.domain.Organization;
import com.quincus.shipment.api.domain.Shipment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
class QLoggerRequestUtilTest {

    @InjectMocks
    private QLoggerPayloadUtil qLoggerPayloadUtil;

    @Test
    void createPayload_AllMandatoryFieldsArePresent_shouldReturnPayloadWithMandatoryFields() {
        Shipment shipment = new Shipment();
        Organization organization = new Organization();
        organization.setId("org-id");
        organization.setCode("org-code");
        shipment.setOrganization(organization);

        QLoggerRequest payload = qLoggerPayloadUtil.createQLoggerPayloadWithMandatoryFields("test#source", QLoggerCategory.SHIPMENT_CREATED, shipment);
        assertThat(payload.getOrganizationId()).isNotNull();
        assertThat(payload.getOrganizationKey()).isNotNull();
        assertThat(payload.getOccuredAt()).isNotNull();
        assertThat(payload.getReportedAt()).isNotNull();
        assertThat(payload.getModule()).isNotNull();
    }
}
