Feature: Cost Shipment Search With Exclude Keys

  Background:
    * def utils = Java.type('com.quincus.karate.automation.utils.DataUtils')
    * url baseUrl
    * def bearer = token
    * def jsonPath = 'classpath:features/shipment/json/bulk/addBulkRQ.json'
    * def createBulkShipmentResult = callonce read('classpath:features/shipment/addBulk.feature') {bulkRQPath : '#(jsonPath)'}
    * print createBulkShipmentResult.data[0].shipment.shipment_tracking_id
    * print createBulkShipmentResult.data[0].shipment.shipment_tracking_id

    * def searchRequest1 = read('classpath:features/shipment/json/search/excludeKeysRQ.json')
    * searchRequest1.data.exclude_keys[0] = createBulkShipmentResult.data[0].shipment.shipment_tracking_id
    * searchRequest1.data.exclude_keys[1] = createBulkShipmentResult.data[1].shipment.shipment_tracking_id

    * def searchRequest2 = read('classpath:features/shipment/json/search/excludeKeysRQ.json')
    * searchRequest2.data.keys[0] = createBulkShipmentResult.data[0].shipment.shipment_tracking_id
    * searchRequest2.data.keys[1] = createBulkShipmentResult.data[1].shipment.shipment_tracking_id
    * searchRequest2.data.exclude_keys[0] = createBulkShipmentResult.data[0].shipment.shipment_tracking_id

    * def searchRequest3 = read('classpath:features/shipment/json/search/excludeKeysRQ.json')
    * searchRequest3.data.keys[0] = createBulkShipmentResult.data[0].shipment.shipment_tracking_id
    * searchRequest3.data.keys[1] = createBulkShipmentResult.data[1].shipment.shipment_tracking_id
    * searchRequest3.data.exclude_keys[0] = createBulkShipmentResult.data[0].shipment.shipment_tracking_id
    * searchRequest3.data.exclude_keys[1] = createBulkShipmentResult.data[1].shipment.shipment_tracking_id


    * def pasayUserCredentials = 'classpath:session/json/pasay_location_coverage_user.json'
    * def pasaySession = callonce read('classpath:session/create.feature') {userCredentials : '#(pasayUserCredentials)'}
    * def pasayToken = pasaySession.response.data.token


  @CostShipmentSearch @Regression
  Scenario: Search Cost Shipments with keys and without excludeKeys
    * def bearer = pasayToken
    Given url baseUrl + '/costs/shipments/search'
    And header Authorization = 'Bearer '+ bearer
    And request searchRequest1
    When method POST
    Then status 200

    * def response = $
    * print response

    * def data = response.data
    * match data.result == '#[2]'
    * match data.result[0].shipment_tracking_id == '#notnull'
    * match data.result[1].shipment_tracking_id == '#notnull'


  @CostShipmentSearch @Regression
  Scenario: Search Cost Shipments with keys and excludeKeys
    * def bearer = pasayToken
    Given url baseUrl + '/costs/shipments/search'
    And header Authorization = 'Bearer '+ bearer
    And request searchRequest2
    When method POST
    Then status 200

    * def response = $
    * print response

    * def data = response.data
    * match data.result == '#[1]'
    * match data.total_elements == 1
    * match data.result[0].shipment_tracking_id == '#notnull'


  @CostShipmentSearch @Regression
  Scenario: Search Cost Shipments with keys and excludeKeys
    * def bearer = pasayToken
    Given url baseUrl + '/costs/shipments/search'
    And header Authorization = 'Bearer '+ bearer
    And request searchRequest3
    When method POST
    Then status 200

    * def response = $
    * print response

    * def data = response.data
    * match data.result == '#[0]'
    * match data.total_elements == 0