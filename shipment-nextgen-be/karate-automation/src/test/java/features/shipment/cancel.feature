Feature: Cancel a Shipment Feature

  Background:
    * url baseUrl
    * def bearer = token
    * def jsonPath = 'classpath:features/shipment/json/single/addSingleRQ.json'
    * def createShipmentResult = callonce read('classpath:features/shipment/addSingle.feature') {singleRQPath : '#(jsonPath)'}
    * def id = createShipmentResult.data.id
    * print id

  @ShipmentCancel @Regression
  Scenario: Cancel a Shipment
    Given path '/shipments/cancel/' + id
    And header Authorization = 'Bearer '+ bearer
    When method PATCH
    Then status 200

    * def response = $
    * print response
    * match response.status == 'Shipment Cancelled.'

