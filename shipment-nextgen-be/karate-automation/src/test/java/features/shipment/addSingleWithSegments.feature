Feature: Create a Single Shipment with segment Feature

  Background:
    * url baseUrl
    * def requestPath = karate.get('singleRQPath', 'classpath:features/shipment/json/single/shipment-with-segment.json')
    * def updatedCreateRequest = read(requestPath)
    * updatedCreateRequest.data.shipment_tracking_id = 'SHP' + utils.uuid().substring(0, 12)
    * updatedCreateRequest.data.order.id = utils.uuid()
    * updatedCreateRequest.data.order.order_id_label = utils.uuid().substring(0, 10)

    * def segments = updatedCreateRequest.data.shipment_journey.package_journey_segments

  @ShipmentCreateWithSegmentSingle @SegmentRegression
  Scenario: Create Shipment with Segments
    * def bearer = token
    * def shipmentTrackingID = utils.uuid()
    Given path '/shipments'
    And header Authorization = 'Bearer '+ bearer
    And request updatedCreateRequest
    When method POST
    Then status 200

    * def response = $
    * print response

    * def data = response.data
    * match data.shipment_journey.package_journey_segments == '#notnull'
    * match data.shipment_journey.package_journey_segments == '##array'
    * match data.shipment_journey.package_journey_segments != '#[0]'
