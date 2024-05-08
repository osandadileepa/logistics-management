Feature: Retrieve a Shipment with Package Info only Feature

  Background:
    * def utils = Java.type('com.quincus.karate.automation.utils.DataUtils')
    * url baseUrl
    * def bearer = token
    * def jsonPath = 'classpath:features/shipment/json/single/addSingleRQ.json'
    * def createShipmentResult = callonce read('classpath:features/shipment/addSingle.feature') {singleRQPath : '#(jsonPath)'}
    * def shipment_tracking_id = createShipmentResult.data.shipment_tracking_id
    * def userCredentials = 'classpath:session/json/no_location_coverage.json'
    * def newSession = callonce read('classpath:session/create.feature') {userCredentials : '#(userCredentials)'}
    * def newToken = newSession.response.data.token

  @ShipmentPackageInfoFind @Regression @Permission
  Scenario: Retrieve a Shipment with Package Info only
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipment_tracking_id + '/package-dimension')
    And header Authorization = 'Bearer ' + bearer
    When method GET
    Then status 200

    * def response = $
    * print response
    * match response.data.shipment_tracking_id == shipment_tracking_id
    * match response.data.shipment_package.type == '#present'
    * match response.data.shipment_package.dimension == '#present'

  @ShipmentPackageInfoFind @Regression
  Scenario: Return Not Found Error with in-existing shipment_tracking_id
    * def shipment_tracking_id = 'unknown'
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipment_tracking_id + '/package-dimension?')
    And header Authorization = 'Bearer ' + bearer
    When method GET
    Then status 404

  @ShipmentPackageInfoFind @Regression @Permission
  Scenario: Return Forbidden Error with existing shipment_tracking_id but no location coverage access
    * def bearer = newToken
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipment_tracking_id + '/package-dimension?')
    And header Authorization = 'Bearer ' + bearer
    When method GET
    Then status 403