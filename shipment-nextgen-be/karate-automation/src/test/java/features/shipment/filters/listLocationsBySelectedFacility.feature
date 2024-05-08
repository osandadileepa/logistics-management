Feature: List Locations By Selected Facility Feature

  Background:
    * url baseUrl
    * def bearer = token
    * def listFacilitiesResult = callonce read('classpath:features/shipment/filters/listFacilities.feature')
    * def facilityId = listFacilitiesResult.response.data.result[0].id
    * print facilityId

  @ListLocationsByFacilityId @Regression
  Scenario: List Location Hierarchies By Facility Id
    * def bearer = token
    Given url baseUrl + '/filter/location_hierarchies?facility_id=' + facilityId + '&per_page=10&page=1'
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * print response

    * def result = response.data.result

    * match result != '#[0]'
    * match result[0].id == '#present'
    * match result[0].id == '#notnull'