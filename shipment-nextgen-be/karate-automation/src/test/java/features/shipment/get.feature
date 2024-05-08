Feature: Retrieve a Shipment Feature

  Background:
    * url baseUrl
    * def bearer = token
    * def jsonPath = 'classpath:features/shipment/json/single/addSingleRQ.json'
    * def createShipmentResult = callonce read('classpath:features/shipment/addSingle.feature') {singleRQPath : '#(jsonPath)'}
    * def id = karate.get('existingShipmentId', createShipmentResult.data.id)
    * print id

  @ShipmentFind @Regression
  Scenario: Retrieve a Shipment By Shipment UUID
    Given path '/shipments/' + id
    And header Authorization = 'Bearer ' + bearer
    When method GET
    Then status 200

    * def response = $
    * print response
    * match response.data.id == id
    * match response.data.created_time == '#present'
    * match response.data.created_time == '#notnull'
    * match response.data.last_updated_time == '#present'
    * match response.data.last_updated_time == '#notnull'
    * match response.data.last_updated_time == '#notnull'
    * match response.data.shipment_journey == '#notnull'
    * match response.data.milestone == '#present'



