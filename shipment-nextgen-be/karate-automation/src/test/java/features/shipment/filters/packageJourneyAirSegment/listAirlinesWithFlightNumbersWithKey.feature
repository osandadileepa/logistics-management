Feature: List All Airlines with Flight Numbers by Key Feature

  Background:
    * url baseUrl
    * def bearer = token
    * def createShipmentResult = callonce read('classpath:features/shipment/addSingle.feature')

  @ListAirlinesWithFlightNumbersByKey @Regression
  Scenario: List All Airlines with Flight Numbers by Key
    * def key = '1'
    * def bearer = token
    * def decodedUrl = utils.decodeUrl(baseUrl + '/package-journey-air-segment/filter/airlines/hierarchies' + '?key=' + key + '&per_page=10&page=1&level=2')
    Given url decodedUrl
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
    * match response.data.result[0].children[0].id == '#present'
    * match response.data.result[0].children[0].id == '#notnull'
    * match response.data.result[0].children[0].name == '#present'
    * match response.data.result[0].children[0].name == '#notnull'

  @ListAirlinesWithFlightNumbersByKey @Regression
  Scenario: Return empty response with unknown key
    * def key = 'unknown_key'
    * def bearer = token
    * def decodedUrl = utils.decodeUrl(baseUrl + '/package-journey-air-segment/filter/airlines/hierarchies' + '?key=' + key + '&per_page=10&page=1&level=2')
    Given url decodedUrl
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * print response

    * assert response.data.total_elements == 0