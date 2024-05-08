Feature: Listing and Retrieve a Shipment matching Feature

  Background:
    * url baseUrl
    * def bearer = token
    * def createShipmentFromOrderResult = callonce read('classpath:features/shipment/addSingleFromOMPayload.feature')
    * def shipmentId = createShipmentFromOrderResult.data[0].id

    * def findAllRequest = read('classpath:features/shipment/json/listAllFilterKeysRQ.json')
    * findAllRequest.data.keys[0] = createShipmentFromOrderResult.data[0].shipment_tracking_id
    * def shipmentListResult = callonce read('classpath:features/shipment/list.feature') {singleRQPath : '#(findAllRequest)'}
    * def listShipmentSegmentLength = shipmentListResult.data.result[0].shipment_journey.package_journey_segments.length

  @ShipmentFind @Regression
  Scenario: Retrieve a Shipment with complete segment even missing location hierarchy
    Given path '/shipments/' + shipmentId
    And header Authorization = 'Bearer ' + bearer
    When method GET
    Then status 200

    * def response = $
    * def getShipmentSegmentLength =  response.data.shipment_journey.package_journey_segments.length
    * match getShipmentSegmentLength == listShipmentSegmentLength
