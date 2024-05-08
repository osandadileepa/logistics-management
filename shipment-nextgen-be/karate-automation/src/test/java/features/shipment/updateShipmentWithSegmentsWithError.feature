Feature: Update a Shipment Feature

  Background:
    # Call the create shipment feature then use the returned shipmentTrackingID
    # to call the update shipment API
    * url baseUrl
    * def bearer = token
    * def moreThan65CharsAirlineValue = 'Koninklijke Luchtvaart Maatschappij voor Nederland en KoloniënXXXX'

    * def createShipmentResult = callonce read('classpath:features/shipment/addSingleWithSegments.feature')
    * def shipmentTrackingIdToUpdate = createShipmentResult.response.data.id

    * def requestForUpdate = read('classpath:features/shipment/json/updateShipmentWithSegmentsRQ.json')
    * requestForUpdate.data.id = shipmentTrackingIdToUpdate
    * requestForUpdate.data.shipment_tracking_id = createShipmentResult.data.shipment_tracking_id
    * requestForUpdate.data.order.id = createShipmentResult.data.order.id
    * requestForUpdate.data.shipment_package.id = createShipmentResult.response.data.shipment_package.id
    * requestForUpdate.data.shipment_journey.journey_id = createShipmentResult.response.data.shipment_journey.journey_id
    * requestForUpdate.data.shipment_journey.package_journey_segments[1].airline = moreThan65CharsAirlineValue
    * print requestForUpdate

  @ShipmentUpdateWithSegments @SegmentRegression
  Scenario: Update Shipment
    Given path '/shipments'
    And header Authorization = 'Bearer '+ bearer
    And request requestForUpdate
    When method PUT
    Then status 400

    * def response = $
    * def data = response.data
    * print data

    * match data.field_errors != '#[0]'
    * match data.field_errors[0].field == 'data.shipmentJourney.packageJourneySegments[1].airline'
    * match data.field_errors[0].message == 'size must be between 0 and 65'
    * match data.field_errors[0].code == 'Size'



