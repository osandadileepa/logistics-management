Feature: List All Cities Feature

  Background:
    * url baseUrl
    * def bearer = token
    * def jsonPath = 'classpath:features/shipment/json/bulk/addBulkRQ.json'
    * def createBulkShipmentResult = callonce read('classpath:features/shipment/addBulk.feature') {bulkRQPath : '#(jsonPath)'}

  @ListCities @Regression
  Scenario: List All Cities sort by name
    * def bearer = token
    Given url baseUrl + '/filter/locations?type=CITY&per_page=10&page=1&sort_by=name'
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * print response

    * match response.data.result != '#[0]'
    * match response.data.result[0].type == 'CITY'

  @ListCities @Regression
  Scenario: List All Cities sort by code
    * def bearer = token
    Given url baseUrl + '/filter/locations?type=CITY&per_page=10&page=1&sort_by=code'
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * print response

    * match response.data.result != '#[0]'
    * match response.data.result[0].type == 'CITY'

