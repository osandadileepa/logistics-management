Feature: List All Countries Feature

  Background:
    * url baseUrl
    * def bearer = token
    * def jsonPath = 'classpath:features/shipment/json/bulk/addBulkRQ.json'
    * def createBulkShipmentResult = callonce read('classpath:features/shipment/addBulk.feature') {bulkRQPath : '#(jsonPath)'}

  @ListCountries @Regression
  Scenario: Get Destination Country
    * def bearer = token
    Given url baseUrl + '/filter/locations?type=COUNTRY&per_page=10&page=1&key=AUSTRALIA'
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * print response

    * match response.data.result != '#[0]'
    * match response.data.result[0].type == 'COUNTRY'