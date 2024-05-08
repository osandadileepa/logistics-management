Feature: Default Listing Page Feature

  Background:
    * def utils = Java.type('com.quincus.karate.automation.utils.DataUtils')
    * url baseUrl
    * def bearer = token
    * def jsonPath = 'classpath:features/shipment/json/bulk/addBulkRQ.json'
    * def createBulkShipmentResult = callonce read('classpath:features/shipment/addBulk.feature') {bulkRQPath : '#(jsonPath)'}
    * def findAllRequest = read('classpath:features/shipment/json/listAllFiltersRQ.json')
    * findAllRequest.data.shipment_tracking_id = utils.uuid()
    * print findAllRequest

  @ShipmentFindAll @Regression
  Scenario: Find All Shipments
    * def bearer = token
    * def shipmentTrackingID = utils.uuid()
    Given path '/shipments/list'
    And header Authorization = 'Bearer '+ bearer
    And request findAllRequest
    When method POST
    Then status 200

    * def response = $
    * print response

    * def req = findAllRequest.data
    * def data = response.data
