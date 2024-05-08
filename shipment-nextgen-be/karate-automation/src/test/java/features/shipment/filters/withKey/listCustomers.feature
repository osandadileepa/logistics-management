Feature: List All Customers Feature

  Background:
    * url baseUrl
    * def bearer = token
    * def jsonPath = 'classpath:features/shipment/json/bulk/addBulkRQ.json'
    * def createBulkShipmentResult = callonce read('classpath:features/shipment/addBulk.feature') {bulkRQPath : '#(jsonPath)'}

  @ListCustomers @Regression
  Scenario: List All Customers with Key
    * def bearer = token
    Given url baseUrl + '/filter/customers?per_page=10&page=1&key=CORP'
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * print response

    * match response.data.result != '#[0]'