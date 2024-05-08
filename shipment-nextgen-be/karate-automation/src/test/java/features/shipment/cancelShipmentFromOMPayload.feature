Feature: Cancel Shipment From Order Payload Feature

  Background:
    * url baseUrl
    * def bearer = token
    * def createShipmentResult = callonce read('classpath:features/shipment/addSingleFromOMPayloadWithSameShipmentJourney.feature')
    * def orderId = createShipmentResult.data[0].order.id
    * def shipment_tracking_id = createShipmentResult.data[0].shipment_tracking_id
    * def cancelledShipmentId = createShipmentResult.data[1].id
    * def requestPath = karate.get('singleRQPath', 'classpath:features/shipment/json/omPayload.json')
    * def updatedRequest = read(requestPath)
    * updatedRequest.data.id = orderId
    * updatedRequest.data.shipments[0].shipment_id_label = shipment_tracking_id
    * remove updatedRequest.data.shipments[1]

  @ShipmentCancelFromOMPayload
  @Regression
  Scenario: Send Order Payload with a Shipment removed and check if shipment was cancelled
    * def bearer = token
    Given path '/orders'
    And header Authorization = 'Bearer '+ bearer
    And request updatedRequest
    When method POST
    Then status 200

    Given path '/shipments/' + cancelledShipmentId
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * print response
    * match response.data.status == 'CANCELLED'