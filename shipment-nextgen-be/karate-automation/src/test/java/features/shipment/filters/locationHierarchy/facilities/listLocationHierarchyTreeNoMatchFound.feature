Feature: List of Location Hierarchy Trees With City Nodes

  Background:
    * url baseUrl
    * def bearer = token
    * def listCountriesResult = callonce read('classpath:features/shipment/filters/listStates.feature')
    * def stateId = listCountriesResult.response.data.result[0].id
    * print stateId

  @GetLocationHierarchyTree  @GetLocationHierarchyTreeCity @Regression
  Scenario: Empty LH Tree When No Result Is Found
    * def bearer = token
    Given url baseUrl + '/filter/location_hierarchies?state_id=' + stateId + '&per_page=10&page=1&leve4=1&key=ANonexistentCountry'
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * print response

    * def result = response.data.result
    * def data = response.data

    * def result = response.data.result
    # Check if data has valid count
    * match data.total_elements == 0
    * match data.total_pages == 0
    * match result == '#[0]'
