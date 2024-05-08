Feature: Default Listing Page Feature

  Background:
    * url baseUrl
    * def jsonPath = 'classpath:features/shipment/json/omPayload.json'
    * def createShipmentResult = call read('classpath:features/shipment/addSingleFromOMPayload.feature') {singleRQPath : '#(jsonPath)'}
    * def shipmentWithNoAlert = createShipmentResult.data[0]

    * def jsonPathWithAlert = 'classpath:features/shipment/json/omPayloadWithoutOpsType.json'
    * def shipmentWithAlertResult = call read('classpath:features/shipment/addSingleFromOMPayload.feature') {singleRQPath : '#(jsonPathWithAlert)'}
    * def shipmentWithAlert = shipmentWithAlertResult.data[0]

    * def searchKeys = []
    * searchKeys[0] = shipmentWithNoAlert.shipment_tracking_id
    * searchKeys[1] = shipmentWithAlert.shipment_tracking_id
    * def searchRequest = read('classpath:features/shipment/json/search/keysTrackingIdsRQ.json')
    * searchRequest.data.journey_status = 'PLANNED'
    * searchRequest.data.keys = searchKeys
    * searchRequest.data.size = 10

  @ShipmentFindAll @Regression
  @ShipmentListingPage
  Scenario: Find All Shipments by inserted tracking_id should be listed order by with alerts first
    * def bearer = token
    Given path '/shipments/list'
    And header Authorization = 'Bearer '+ bearer
    And request searchRequest
    When method POST
    Then status 200

    * def response = $
    * match response.data.result[0].shipment_journey.alerts == '#[1]'
    * match response.data.result[1].shipment_journey.alerts == '#[0]'
