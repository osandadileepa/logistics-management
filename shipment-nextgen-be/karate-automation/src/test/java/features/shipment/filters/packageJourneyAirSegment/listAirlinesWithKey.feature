Feature: List All Airlines by Key Feature

  Background:
    * def utils = Java.type('com.quincus.karate.automation.utils.DataUtils')
    * url baseUrl
    * def bearer = token
    * def createSingleShipmentResult = callonce read('classpath:features/shipment/addSingle.feature')
    * print createSingleShipmentResult

  @ListAirlinesByKey @Regression
  Scenario: List All Flight Numbers with Key and level 1 for airline name
    * def key = 's'
    * def bearer = token
    * def decodedUrl = utils.decodeUrl(baseUrl + '/package-journey-air-segment/filter/airlines/hierarchies' + '?key=' + key + '&per_page=10&page=1&level=1')
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
    * match response.data.result[0].children == '#notpresent'

  @ListAirlinesByKey @Regression
  Scenario: List All Flight Numbers with Key and level 1 for airline code
    * def key = 'S'
    * def bearer = token
    * def decodedUrl = utils.decodeUrl(baseUrl + '/package-journey-air-segment/filter/airlines/hierarchies' + '?key=' + key + '&per_page=10&page=1&level=1')
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
    * match response.data.result[0].children == '#notpresent'

  @ListAirlinesByKey @Regression
  Scenario: Return empty response with unknown key
    * def key = 'unknown_key'
    * def bearer = token
    * def decodedUrl = utils.decodeUrl(baseUrl + '/package-journey-air-segment/filter/airlines/hierarchies' + '?key=' + key + '&per_page=10&page=1&level=1')
    Given url decodedUrl
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * print response

    * assert response.data.total_elements == 0