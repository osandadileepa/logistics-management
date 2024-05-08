Feature: List All Cities Feature

  Background:
    * url baseUrl
    * def bearer = token
    * def jsonPath = 'classpath:features/shipment/json/bulk/addBulkRQ.json'
    * def createBulkShipmentResult = callonce read('classpath:features/shipment/addBulk.feature') {bulkRQPath : '#(jsonPath)'}

  @ListCities @Regression
  Scenario: List All Cities with Key
    * def bearer = token
    Given url baseUrl + '/filter/locations?type=CITY&per_page=10&page=1&key=PASAY'
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * print response

    * match response.data.result != '#[0]'
    * match response.data.result[0].type == 'CITY'