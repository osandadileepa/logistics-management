Feature: List of Location Hierarchy Tress with State Nodes

  Background:
    * url baseUrl
    * def bearer = token
    * def listCountriesResult = callonce read('classpath:features/shipment/filters/listCountries.feature')
    * def organizationId = listCountriesResult.response.data.result[0].organization_id
    * def countryName1 = listCountriesResult.response.data.result[0].name
    * def countryName2 = listCountriesResult.response.data.result[1].name
    * print organizationId

  @GetLocationHierarchyTree  @GetLocationHierarchyTreeState @Regression
  Scenario:  List of Location Hierarchy Tress with State Nodes
    * def bearer = token
    Given url baseUrl + '/filter/location_hierarchies?per_page=10&page=1&level=2'
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * print response

    * def result = response.data.result
    * def data = response.data

    * def result = response.data.result
    # Check if data has valid count
    * match data.total_elements != '#[0]'
    * match data.total_pages != '#[0]'

    * match result != '#[0]'
    # Ensure that they are in ascending order based on country name
    * match result[0].type == 'COUNTRY'
    * match result[0].name == countryName1
    # Ensure that response has state values
    * match result[0].children == '#[1]'

    * def state = result[0].children[0]
    * match state.name == 'NEW SOUTH WALES'