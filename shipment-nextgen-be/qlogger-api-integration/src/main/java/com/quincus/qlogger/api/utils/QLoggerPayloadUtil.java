package com.quincus.qlogger.api.utils;

import com.quincus.qlogger.api.QLoggerCategory;
import com.quincus.qlogger.api.QLoggerConstants;
import com.quincus.qlogger.model.QLoggerRequest;
import com.quincus.shipment.api.domain.Cost;
import com.quincus.shipment.api.domain.Shipment;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
@AllArgsConstructor
public class QLoggerPayloadUtil {

    /**
     * Create Qlogger payload request with all the mandatory fields
     *
     * @param source
     * @param category
     * @param shipment
     * @return QloggerPayload with mandatory fields
     */
    public QLoggerRequest createQLoggerPayloadWithMandatoryFields(final String source, final QLoggerCategory category, final Shipment shipment) {
        QLoggerRequest qLoggerRequest = createQLoggerPayloadWithMandatoryFields(source, category);
        if (shipment != null) {
            qLoggerRequest.setOrganizationId(shipment.getOrganization().getId());
            qLoggerRequest.setOrganizationKey(shipment.getOrganization().getCode());
            qLoggerRequest.setShipmentAttribute(shipment);
        }
        return qLoggerRequest;
    }

    public QLoggerRequest createQLoggerPayloadWithMandatoryFields(final String source, final QLoggerCategory category, final Cost cost) {
        QLoggerRequest qLoggerRequest = createQLoggerPayloadWithMandatoryFields(source, category);
        if (cost != null) {
            qLoggerRequest.setOrganizationId(cost.getOrganizationId());
        }
        return qLoggerRequest;
    }

    public QLoggerRequest createQLoggerPayloadWithMandatoryFields(final String source, final QLoggerCategory category) {
        QLoggerRequest qLoggerRequest = new QLoggerRequest();
        qLoggerRequest.setSource(source);
        qLoggerRequest.setCategory(category.toString());
        String dateNow = LocalDateTime.now().toString();
        qLoggerRequest.setOccuredAt(dateNow);
        qLoggerRequest.setReportedAt(dateNow);
        qLoggerRequest.setModule(QLoggerConstants.SHIPMENT_MODULE);
        return qLoggerRequest;
    }

}
