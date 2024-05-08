Feature: List of Location Hierarchy Tress with State Nodes

  Background:
    * url baseUrl
    * def bearer = token
    * def listCountriesResult = callonce read('classpath:features/shipment/filters/listCountries.feature')
    * print listCountriesResult
    * def organizationId = listCountriesResult.response.data.result[0].organization_id
    * def countryId = listCountriesResult.response.data.result[0].id
    * print organizationId
    * print countryId

  @GetLocationHierarchyTree  @GetLocationHierarchyTreeState @Regression
  Scenario:  List of Location Hierarchy Tress with State Nodes Given Country Id
    * def bearer = token
    Given url baseUrl + '/filter/location_hierarchies?country_id=' + countryId + '&per_page=10&page=1&level=2'
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * print response

    * def result = response.data.result
    * def data = response.data

    * def result = response.data.result
    # Check if data has valid count
    * match data.total_elements == 1
    * match data.total_pages == 1

    * match result != '#[0]'
    # Ensure that they are in ascending order based on country name
    * match result[0].type == 'COUNTRY'
    * match result[0].id == countryId
    # Ensure that only countries are returned
    * match result[0].children == '#[1]'

    * def state = result[0].children[0]
    * match state.name == 'NEW SOUTH WALES'