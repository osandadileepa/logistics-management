Feature: Default Listing Page Feature With User No Location Coverage

  Background:
    * def utils = Java.type('com.quincus.karate.automation.utils.DataUtils')
    * url baseUrl
    * def bearer = token
    * def jsonPath = 'classpath:features/shipment/json/bulk/addBulkRQ.json'
    * def createBulkShipmentResult = callonce read('classpath:features/shipment/addBulk.feature') {bulkRQPath : '#(jsonPath)'}
    * def findAllRequest = read('classpath:features/shipment/json/listRQ.json')
    * findAllRequest.data.shipment_tracking_id = utils.uuid()

    * def userCredentials = 'classpath:session/json/no_location_coverage.json'
    * def newSession = callonce read('classpath:session/create.feature') {userCredentials : '#(userCredentials)'}
    * def newToken = newSession.response.data.token

  @ShipmentFindAll @Regression
  @ShipmentListingPage
  Scenario: Find All Shipments with user no location coverage
    * def bearer = newToken
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

    * assert data.total_elements == 0
    * assert data.total_pages == 0
    * match data.result[0] == '#notpresent'


