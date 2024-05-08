Feature: Default Listing Page Feature

  Background:
    * def utils = Java.type('com.quincus.karate.automation.utils.DataUtils')
    * url baseUrl
    * def bearer = token
    * def findAllRequest = read('classpath:features/shipment/json/listRQ.json')
    * findAllRequest.data.size = 2147483642
    * print findAllRequest

  @ShipmentFindAll @Regression
  @ShipmentListingPage
  @ShipmentFindAllWithInvalidRequest
  Scenario: Find All Shipments - Failing with Invalid Request
    * def bearer = token
    * def shipmentTrackingID = utils.uuid()
    Given path '/shipments/list'
    And header Authorization = 'Bearer '+ bearer
    And request findAllRequest
    When method POST
    Then status 400

    * def response = $
    * print response


  @Regression
  @ShipmentExportWithInvalidRequest
  Scenario: Export Shipments - Failing with Invalid Request
    * def bearer = token
    * def exportRequest = findAllRequest
    * exportRequest.data.destination = []
    * exportRequest.data.destination.push('EX-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5')
    * exportRequest.data.order = {status: 'EX-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5-BLBAT2JFZN4Y43X06JKLK73EIZOB1CE5'}
    Given path '/shipments/export'
    And header Authorization = 'Bearer '+ bearer
    And request exportRequest
    When method POST
    Then status 400

    * def response = $
    * print response
