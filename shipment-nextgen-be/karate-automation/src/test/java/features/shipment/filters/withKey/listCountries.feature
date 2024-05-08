Feature: List All Countries Feature

  Background:
    * url baseUrl
    * def bearer = token
    * def result = callonce read('classpath:features/shipment/filters/listCountries.feature')
    * print result
    * def key = result.listCountriesResponse.data.result[0].name

  @ListCountries @Regression
  Scenario: List All Countries with Key
    * def bearer = token
    Given url baseUrl + '/filter/locations?type=COUNTRY&per_page=10&page=1&key=' + key
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * print response

    * match response.data.result != '#[0]'
    * match response.data.result[0].name == key
    * match response.data.result[0].type == 'COUNTRY'