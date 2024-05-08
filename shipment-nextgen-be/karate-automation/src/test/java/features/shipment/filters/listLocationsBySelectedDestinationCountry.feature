Feature: List Locations By Selected Country Feature

  Background:
    * url baseUrl
    * def bearer = token
    * def listCountriesResult = callonce read('classpath:features/shipment/filters/withKey/listDestinationCountries.feature')
    * def countryId = listCountriesResult.response.data.result[0].id
    * print countryId

  @ListLocationsByCountryId @Regression
  Scenario: List Location Hierarchies By Country Id
    * def bearer = token
    Given url baseUrl + '/filter/location_hierarchies?country_id=' + countryId + '&per_page=10&page=1'
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * print response

    * def result = response.data.result

    * match result != '#[0]'
    * match result[0].id == '#present'
    * match result[0].id == '#notnull'
    * match result[0].id == countryId