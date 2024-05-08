Feature: Create Shipments from OM Payload Feature

  Background:
    * url baseUrl
    * def bearer = token
    * def createShipmentFromOrderResult = callonce read('classpath:features/shipment/addSingleFromOMPayload.feature')
    * print createShipmentFromOrderResult
    * def orderId = createShipmentFromOrderResult.data[0].order.id
    * def shipmentCode = createShipmentFromOrderResult.data[0].shipment_code
    * def requestPath = karate.get('singleRQPath', 'classpath:features/shipment/json/omPayload.json')
    * def updatedRequest = read(requestPath)
    * updatedRequest.data.id = orderId
    * updatedRequest.data.shipments[0].order_id = orderId
    * updatedRequest.data.shipments[1].order_id = orderId
    * updatedRequest.data.shipments[0].shipment_id_label = createShipmentFromOrderResult.data[0].shipment_tracking_id
    * updatedRequest.data.shipments[1].shipment_id_label = createShipmentFromOrderResult.data[1].shipment_tracking_id
    * def expectedSegmentIDs = karate.jsonPath(createShipmentFromOrderResult, "data[*].shipment_journey.package_journey_segments[*].segment_id")

  @ShipmentCreateFromOMPayload
  @Regression
  Scenario: Create Then Update Shipments Detail from OM Payload With Segments Not Updated
    * def bearer = token
    * updatedRequest.data.shipment_code = shipmentCode
    * updatedRequest.data.segments_updated = false
    * def expectedOrderNote = 'New updated note'
    * updatedRequest.data.note = expectedOrderNote
    Given path '/orders'
    And header Authorization = 'Bearer '+ bearer
    And request updatedRequest
    When method POST
    Then status 200

    * def response = $

    * def data = response.data
    * print response.data[0]

    * match data == '#present'
    * match data == '#notnull'
    * match data == '##array'
    * match data == '#[2]'

    * match response.data[0].shipment_journey == '#present'
    * match response.data[0].shipment_journey == '#notnull'
    * match response.data[0].shipment_journey.journey_id == createShipmentFromOrderResult.data[0].shipment_journey.journey_id
    * match expectedSegmentIDs contains response.data[0].shipment_journey.package_journey_segments[0].segment_id
    * match expectedSegmentIDs contains response.data[0].shipment_journey.package_journey_segments[1].segment_id
    * match expectedSegmentIDs contains response.data[0].shipment_journey.package_journey_segments[2].segment_id
    * match response.data[0].order.notes == '#present'
    * match response.data[0].order.notes == '#notnull'
    * match response.data[0].order.notes == expectedOrderNote
    * match response.data[0].order.id == '#present'
    * match response.data[0].order.id == '#notnull'
    * match response.data[0].order.id == orderId

    * match response.data[1].shipment_journey == '#notnull'
    * match response.data[1].shipment_journey == '#present'
    * match response.data[1].shipment_journey.journey_id == createShipmentFromOrderResult.data[1].shipment_journey.journey_id
    * match expectedSegmentIDs contains response.data[1].shipment_journey.package_journey_segments[0].segment_id
    * match expectedSegmentIDs contains response.data[1].shipment_journey.package_journey_segments[1].segment_id
    * match expectedSegmentIDs contains response.data[1].shipment_journey.package_journey_segments[2].segment_id
    * match response.data[1].order.notes == '#present'
    * match response.data[1].order.notes == '#notnull'
    * match response.data[1].order.notes == expectedOrderNote
    * match response.data[1].order.id == '#present'
    * match response.data[1].order.id == '#notnull'
    * match response.data[1].order.id == orderId

