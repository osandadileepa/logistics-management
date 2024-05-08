Feature: Retrieve a Shipment Journey Feature

  Background:
    * url baseUrl
    * def bearer = token
    * def jsonPath = karate.get('createRQ', 'classpath:features/shipment/json/single/addSingleRQ.json')
    * def createShipmentResult = callonce read('classpath:features/shipment/addSingle.feature') {singleRQPath : '#(jsonPath)'}
    * def shipmentId = createShipmentResult.data.id
    * print shipmentId

  @ShipmentJourneyFind @Regression
  Scenario: Retrieve a Shipment Journey
    Given path '/shipments/' + shipmentId + '/shipment_journey'
    And header Authorization = 'Bearer ' + bearer
    When method GET
    Then status 200

    * def response = $
    * print response
    * match response.data.journey_id == '#present'
    * match response.data.journey_id == '#notnull'
    * match response.data.order_id == '#present'
    * match response.data.order_id == '#notnull'
