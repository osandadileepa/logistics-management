Feature: List All Facilities Feature

  Background:
    * url baseUrl
    * def bearer = token
    * def jsonPath = 'classpath:features/shipment/json/bulk/addBulkRQ.json'
    * def createBulkShipmentResult = callonce read('classpath:features/shipment/addBulk.feature') {bulkRQPath : '#(jsonPath)'}

  @ListFacilities @Regression
  Scenario: List All Facilities with Name contains Key
    * def bearer = token
    Given url baseUrl + '/filter/locations?type=FACILITY&per_page=10&page=1&key=SG'
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * print response

    * match response.data.result != '#[0]'

  @ListFacilities @Regression
  Scenario: List All Facilities with Code contains Key
    * def bearer = token
    Given url baseUrl + '/filter/locations?type=FACILITY&per_page=10&page=1&key=AUSTRALIA'
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * print response

    * match response.data.result != '#[0]'

  @ListFacilities @Regression
  Scenario: List All Facilities with Name or Code does not contains Key
    * def bearer = token
    Given url baseUrl + '/filter/locations?type=FACILITY&per_page=10&page=1&key=unknown'
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * print response

    * match response.data.result == '#[0]'
