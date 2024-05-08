Feature: List All States Feature

  Background:
    * url baseUrl
    * def bearer = token
    * def jsonPath = 'classpath:features/shipment/json/bulk/addBulkRQ.json'
    * def createBulkShipmentResult = callonce read('classpath:features/shipment/addBulk.feature') {bulkRQPath : '#(jsonPath)'}

  @ListStates @Regression
  Scenario: List All States sort by name
    * def bearer = token
    Given url baseUrl + '/filter/locations?type=STATE&per_page=10&page=1&sort_by=name'
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * print response

    * match response.data.result != '#[0]'
    * match response.data.result[0].type == 'STATE'
    * def milestoneName = response.data.result[0].name
    * match milestoneName.toUpperCase() == '#notnull'

  @ListStates @Regression
  Scenario: List All States sort by code
    * def bearer = token
    Given url baseUrl + '/filter/locations?type=STATE&per_page=10&page=1&sort_by=code'
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * print response

    * match response.data.result != '#[0]'
    * match response.data.result[0].type == 'STATE'
    * def milestoneCode = response.data.result[0].code
    * match milestoneCode.toUpperCase() == '#notnull'