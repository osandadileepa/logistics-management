Feature: List All Countries Feature

  Background:
    * url baseUrl
    * def bearer = token
    * def jsonPath = 'classpath:features/shipment/json/bulk/addBulkRQ.json'
    * def createBulkShipmentResult = callonce read('classpath:features/shipment/addBulk.feature') {bulkRQPath : '#(jsonPath)'}
    * def startFacility = createBulkShipmentResult.data[0].shipment.shipment_journey.package_journey_segments[0].start_facility
    * def countryId = startFacility.location.country
    * print countryId

  @ListCountries @Regression
  Scenario: List All Countries sort by name
    * def bearer = token
    Given url baseUrl + '/filter/locations?type=COUNTRY&per_page=10&page=1&sort_by=name'
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * print response

    * match response.data.result != '#[0]'
    * match response.data.result[0].type == 'COUNTRY'

    * match response.data.result contains deep { name : 'AUSTRALIA' }

  @ListCountries @Regression
  Scenario: List All Countries sort by code
    * def bearer = token
    Given url baseUrl + '/filter/locations?type=COUNTRY&per_page=10&page=1&sort_by=code'
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def listCountriesResponse = $
    * print listCountriesResponse

    * match listCountriesResponse.data.result != '#[0]'
    * match listCountriesResponse.data.result[0].type == 'COUNTRY'

    * match response.data.result contains deep { code : 'AUSTRALIA' }