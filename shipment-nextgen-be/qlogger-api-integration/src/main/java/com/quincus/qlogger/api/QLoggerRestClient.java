package com.quincus.qlogger.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.qlogger.api.utils.QLoggerPayloadUtil;
import com.quincus.qlogger.config.QLoggerProperties;
import com.quincus.qlogger.model.QLoggerRequest;
import com.quincus.qlogger.model.QLoggerResponse;
import com.quincus.shipment.api.domain.Cost;
import com.quincus.shipment.api.domain.Organization;
import com.quincus.shipment.api.domain.Package;
import com.quincus.shipment.api.domain.PackageDimension;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class QLoggerRestClient {

    private static final String API_CALL_ERROR = "Encountered an exception while publishing qlogger event `{}`";
    private final QLoggerProperties qLoggerProperties;
    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper;
    private final QLoggerPayloadUtil qLoggerPayloadUtil;

    private String getUrl(String api) {
        return qLoggerProperties.getBaseUrl() + api;
    }

    private ResponseEntity<QLoggerResponse> publishEvent(final QLoggerRequest qLoggerRequest) {
        String url = getUrl(qLoggerProperties.getPublishEventAPI());
        try {
            HttpEntity<String> request = generateRequest(qLoggerRequest);
            ResponseEntity<QLoggerResponse> response = restTemplate.exchange(url, HttpMethod.POST, request, QLoggerResponse.class);
            log.info("Response from qlogger API : {}", response);
            return response;
        } catch (Exception exception) {
            log.error(API_CALL_ERROR + exception.getMessage());
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    /**
     * Publish a qlogger event when a shipment journey has been updated ( this includes deletion, cancellation and or creation of new segments)
     *
     * @param source
     * @param previousShipmentJourney
     * @param newShipmentJourney
     * @param shipment
     *
     * @return
     */
    public ResponseEntity<QLoggerResponse> publishShipmentJourneyUpdatedEvent(String source, ShipmentJourney previousShipmentJourney, ShipmentJourney newShipmentJourney, Shipment shipment) {
        QLoggerRequest qLoggerRequest = qLoggerPayloadUtil.createQLoggerPayloadWithMandatoryFields(source, QLoggerCategory.SEGMENT_JOURNEY_UPDATED, shipment);
        qLoggerRequest.setShipmentAttribute(null); // not required for shipment journey updated events
        qLoggerRequest.setSegmentJourneyPreviousAttributes(previousShipmentJourney);
        qLoggerRequest.setSegmentJourneyNewAttributes(newShipmentJourney);
        return publishEvent(qLoggerRequest);
    }

    /**
     * Publish a qlogger event when a shipment journey has been created
     *
     * @param source
     * @param newShipmentJourney
     * @param shipment
     *
     * @return
     */
    public ResponseEntity<QLoggerResponse> publishShipmentJourneyCreatedEvent(String source, ShipmentJourney newShipmentJourney, Shipment shipment) {
        QLoggerRequest qLoggerRequest = qLoggerPayloadUtil.createQLoggerPayloadWithMandatoryFields(source, QLoggerCategory.SEGMENT_JOURNEY_CREATED, shipment);
        qLoggerRequest.setShipmentAttribute(null); // not required for new shipment journey
        qLoggerRequest.setSegmentJourneyNewAttributes(newShipmentJourney);
        return publishEvent(qLoggerRequest);
    }

    /**
     * Publish a qlogger event when a shipment has been created
     * as per the qlogger wiki, we need to set shipment_attributes ,
     * which has the properties of the shipment we have added
     * see <a href="https://quincus.atlassian.net/wiki/spaces/AD/pages/1123418162/qLogger+categories+definitions+Shipment#The-shipment_created-category">...</a>
     *
     * @param source   Identification of the component generating the data.
     *                 The developer decides how to identify the source of the event on a case-by-case basis.
     * @param shipment The shipment domain
     *
     * @return QLoggerResponse response from qlogger API
     */
    public ResponseEntity<QLoggerResponse> publishShipmentCreatedEvent(final String source, final Shipment shipment) {
        QLoggerRequest qLoggerRequest = qLoggerPayloadUtil.createQLoggerPayloadWithMandatoryFields(source, QLoggerCategory.SHIPMENT_CREATED, shipment);
        return publishEvent(qLoggerRequest);
    }

    /**
     * Publish an event where the shipment has been updated
     *
     * @param source   Identification of the component generating the data.
     *                 The developer decides how to identify the source of the event on a case-by-case basis.
     * @param shipment The updated shipment
     *
     * @return ResponseEntity<QLoggerAPIResponse> response from qlogger API
     */
    public ResponseEntity<QLoggerResponse> publishShipmentUpdatedEvent(final String source, final Shipment shipment) {
        QLoggerRequest qLoggerRequest = qLoggerPayloadUtil.createQLoggerPayloadWithMandatoryFields(source, QLoggerCategory.SHIPMENT_UPDATED, shipment);
        return publishEvent(qLoggerRequest);
    }

    /**
     * Publish an event where the shipment has been cancelled
     *
     * @param source   Identification of the component generating the data.
     *                 The developer decides how to identify the source of the event on a case-by-case basis.
     * @param shipment The cancelled shipment
     *
     * @return ResponseEntity<QLoggerAPIResponse> response from qlogger API
     */
    public ResponseEntity<QLoggerResponse> publishShipmentCancelledEvent(final String source, final Shipment shipment) {
        QLoggerRequest qLoggerRequest = qLoggerPayloadUtil.createQLoggerPayloadWithMandatoryFields(source, QLoggerCategory.SHIPMENT_CANCELLED, shipment);
        qLoggerRequest.setCustomData(QLoggerConstants.SHIPMENT_CANCELLED_CUSTOM_DATA_TEXT + shipment.getId());
        return publishEvent(qLoggerRequest);
    }

    /**
     * Publish an event when a shipment has been exported.
     * As per the qlogger wiki, we need to set shipment_csv_file
     * see <a href="https://quincus.atlassian.net/wiki/spaces/AD/pages/1123418162/qLogger+categories+definitions+Shipment#The-shipment_exported-category">...</a>
     *
     * @param source       Identification of the component generating the data. The developer decides how to identify the source of the event on a case-by-case basis.
     * @param organization Organization that triggered the event
     * @param fileContent  the csv file content
     *
     * @return ResponseEntity<QLoggerAPIResponse> response from qlogger API
     */
    public ResponseEntity<QLoggerResponse> publishShipmentExportedEvent(final String source, final Organization organization, final String fileContent) {
        Shipment shipment = new Shipment();
        shipment.setOrganization(organization);
        QLoggerRequest qLoggerRequest = qLoggerPayloadUtil.createQLoggerPayloadWithMandatoryFields(source, QLoggerCategory.SHIPMENT_EXPORTED, shipment);
        qLoggerRequest.setShipmentCsvFile(fileContent);
        return publishEvent(qLoggerRequest);
    }


    public ResponseEntity<QLoggerResponse> publishPackageDimensionUpdateEvent(final String source, final PackageDimension oldDimension, final Shipment shipment) {
        QLoggerRequest qLoggerRequest = qLoggerPayloadUtil.createQLoggerPayloadWithMandatoryFields(source, QLoggerCategory.PACKAGE_DIMENSION_UPDATED, shipment);
        qLoggerRequest.setPackageAttributes(List.of(shipment.getShipmentPackage()));
        qLoggerRequest.setPreviousDimensions(List.of(oldDimension));
        qLoggerRequest.setNewDimensions(List.of(shipment.getShipmentPackage().getDimension()));
        return publishEvent(qLoggerRequest);
    }

    private HttpEntity<String> generateRequest(final QLoggerRequest qLoggerRequest) throws JsonProcessingException {
        final String body = objectMapper.writer().withRootName(QLoggerConstants.ROOT_NAME).writeValueAsString(qLoggerRequest);
        HttpHeaders headers = generateHeaders(qLoggerRequest, body);
        return new HttpEntity<>(body, headers);
    }

    private HttpHeaders generateHeaders(final QLoggerRequest qLoggerRequest, final String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(QLoggerConstants.AUTHORIZATION, qLoggerProperties.getS2sToken());
        if (qLoggerRequest.getOrganizationId() != null) {
            headers.add(QLoggerConstants.ORGANIZATION_ID, qLoggerRequest.getOrganizationId());
        }
        headers.setContentLength(body.length());
        return headers;
    }

    public ResponseEntity<QLoggerResponse> publishBulkPackageDimensionUpdateEvent(String source, List<Package> packageAttributes, List<PackageDimension> oldDimensions, List<PackageDimension> newDimensions) {
        QLoggerRequest qLoggerPayload = qLoggerPayloadUtil.createQLoggerPayloadWithMandatoryFields(source, QLoggerCategory.PACKAGE_DIMENSION_BULKUPDATED);
        qLoggerPayload.setPackageAttributes(packageAttributes);
        qLoggerPayload.setPreviousDimensions(oldDimensions);
        qLoggerPayload.setNewDimensions(newDimensions);
        return publishEvent(qLoggerPayload);
    }

    /**
     * Publish a qlogger event when a cost has been created
     *
     * @param source Identification of the component generating the data.
     *               The developer decides how to identify the source of the event on a case-by-case basis.
     * @param cost   The shipment cost
     */
    public ResponseEntity<QLoggerResponse> publishCostCreatedEvent(final String source, final Cost cost) {
        QLoggerRequest qLoggerRequest = qLoggerPayloadUtil.createQLoggerPayloadWithMandatoryFields(source, QLoggerCategory.COST_CREATED, cost);
        return publishEvent(qLoggerRequest);
    }

    /**
     * Publish an event where the cost has been updated
     *
     * @param source Identification of the component generating the data.
     *               The developer decides how to identify the source of the event on a case-by-case basis.
     * @param cost   The updated cost
     */
    public ResponseEntity<QLoggerResponse> publishCostUpdatedEvent(final String source, final Cost cost) {
        QLoggerRequest qLoggerRequest = qLoggerPayloadUtil.createQLoggerPayloadWithMandatoryFields(source, QLoggerCategory.COST_UPDATED, cost);
        return publishEvent(qLoggerRequest);
    }

    public ResponseEntity<QLoggerResponse> publishVendorBookingUpdateEvent(String source, Shipment shipment, PackageJourneySegment oldPackageJourneySegment, PackageJourneySegment newPackageJourneySegment) {
        QLoggerRequest qLoggerRequest = qLoggerPayloadUtil.createQLoggerPayloadWithMandatoryFields(source, QLoggerCategory.VENDOR_BOOKING_UPDATED, shipment);
        qLoggerRequest.setOldPackageJourneySegment(oldPackageJourneySegment);
        qLoggerRequest.setNewPackageJourneySegment(newPackageJourneySegment);
        return publishEvent(qLoggerRequest);
    }
}
