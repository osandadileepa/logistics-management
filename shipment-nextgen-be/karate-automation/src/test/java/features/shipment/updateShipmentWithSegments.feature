Feature: Update a Shipment Feature

  Background:
    # Call the create shipment feature then use the returned shipmentTrackingID
    # to call the update shipment API
    * url baseUrl
    * def bearer = token
    * def givenConsigneeNameValue = 'James'
    * def givenAirlineValue = 'Koninklijke Luchtvaart Maatschappij voor Nederland'

    * def createShipmentResult = callonce read('classpath:features/shipment/addSingleWithSegments.feature')
    * def shipmentTrackingIdToUpdate = createShipmentResult.response.data.id

    * def requestForUpdate = read('classpath:features/shipment/json/updateShipmentWithSegmentsRQ.json')
    * requestForUpdate.data.id = shipmentTrackingIdToUpdate
    * requestForUpdate.data.shipment_tracking_id = createShipmentResult.data.shipment_tracking_id
    * requestForUpdate.data.order.id = createShipmentResult.data.order.id
    * requestForUpdate.data.shipment_package.id = createShipmentResult.response.data.shipment_package.id
    * requestForUpdate.data.shipment_journey.journey_id = createShipmentResult.response.data.shipment_journey.journey_id
    * requestForUpdate.data.shipment_journey.package_journey_segments[2].airline = givenAirlineValue
    * requestForUpdate.data.consignee.name = givenConsigneeNameValue

    * print requestForUpdate

  @ShipmentUpdateWithSegments @SegmentRegression
  Scenario: Update Shipment Details Except Shipment Journey
    Given path '/shipments'
    And header Authorization = 'Bearer '+ bearer
    And request requestForUpdate
    When method PUT
    Then status 200

    * def response = $
    * print response

    * match response.data.id == shipmentTrackingIdToUpdate
    * match response.data.consignee.name == givenConsigneeNameValue
    * match response.data.shipment_journey.package_journey_segments[2].airline != givenAirlineValue



