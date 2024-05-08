Feature: Default Listing Page Feature

  Background:
    * def utils = Java.type('com.quincus.karate.automation.utils.DataUtils')
    * url baseUrl
    * def bearer = token
    * def jsonPath = 'classpath:features/shipment/json/bulk/addBulkRQ.json'
    * def createBulkShipmentResult = callonce read('classpath:features/shipment/addBulk.feature') {bulkRQPath : '#(jsonPath)'}
    * def findAllRequest = read('classpath:features/shipment/json/listRQ.json')
    * findAllRequest.data.shipment_tracking_id = utils.uuid()
    * print findAllRequest

  @ShipmentFindAll @Regression
  @ShipmentListingPage
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

    * assert data.total_elements >= 1
    * assert data.total_pages >= 1
    * match data.filter.size == req.size
    * match data.current_page == req.page_number
    * match data.result == '#[1]'

    * match data.result[0].id == '#present'
    * match data.result[0].origin == '#present'
    * match data.result[0].destination == '#present'
    * match data.result[0].shipment_tracking_id == '#present'
    * match data.result[0].shipment_journey == '#present'