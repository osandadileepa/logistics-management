Feature: List All Airlines Feature

  Background:
    * url baseUrl
    * def bearer = token
    * def createShipmentResult = callonce read('classpath:features/shipment/addSingle.feature')

  @ListAirlines @Regression
  Scenario: List All Airlines
    * def bearer = token
    Given url baseUrl + '/package-journey-air-segment/filter/airlines?per_page=10&page=1'
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * print response

    * match response.data.result != '#[0]'
    * match response.data.result[0].id == '#present'
    * match response.data.result[0].id == '#notnull'
    * match response.data.result[0].name == '#present'
    * match response.data.result[0].name == '#notnull'
    * match response.data.result[0].code == '#present'
    * match response.data.result[0].code == '#notnull'
    * match response.data.result[0].children == '#notpresent'