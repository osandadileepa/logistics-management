Feature: Cost Shipment Search With No User Location Coverage Specifics

  Background:
    * def utils = Java.type('com.quincus.karate.automation.utils.DataUtils')
    * url baseUrl
    * def bearer = token
    * def jsonPath = 'classpath:features/shipment/json/bulk/addBulkRQ.json'
    * def requestPath = karate.get('singleRQPath', 'classpath:features/shipment/json/single/addSingleRQ.json')
    * def createShipmentResult = callonce read('classpath:features/shipment/addSingleWithUserPartner001.feature') {singleRQPath : '#(requestPath)'}

    * def searchRequest = read('classpath:features/shipment/json/search/keysTrackingIdsRQ.json')
    * searchRequest.data.keys[0] = createShipmentResult.data.shipment_tracking_id

    * def userCredentials = 'classpath:session/json/no_location_coverage.json'
    * def newSession = callonce read('classpath:session/create.feature') {userCredentials : '#(userCredentials)'}
    * def newToken = newSession.response.data.token

  @CostShipmentSearch @Regression
  Scenario: Cost Shipment Search with shipment_tracking_id without location coverage
    * def bearer = newToken
    Given url baseUrl + '/costs/shipments/search'
    And header Authorization = 'Bearer '+ bearer
    And request searchRequest
    When method POST
    Then status 200

    * def response = $
    * print response

    * def data = response.data

    * assert data.total_elements == 0
    * assert data.total_pages == 0
    * match data.result[0] == '#notpresent'

