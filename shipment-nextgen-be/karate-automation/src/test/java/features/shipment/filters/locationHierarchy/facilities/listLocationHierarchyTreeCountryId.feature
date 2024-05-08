Feature: List of Location Hierarchy Trees With Facility Nodes

  Background:
    * url baseUrl
    * def bearer = token
    * def listCountriesResult = callonce read('classpath:features/shipment/filters/listCountries.feature')
    * print listCountriesResult
    * def organizationId = listCountriesResult.response.data.result[0].organization_id
    * print organizationId
    * def countryId = listCountriesResult.response.data.result[0].id
    * print organizationId
    * print countryId

  @GetLocationHierarchyTree  @GetLocationHierarchyTreeFacility @Regression
  Scenario: List Location Hierarchies Tree With Facility Nodes Given Country Id
    * def bearer = token
    Given url baseUrl + '/filter/location_hierarchies?country_id=' + countryId + '&per_page=10&page=1&level=4'
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
    # Ensure that response has state values
    * match result[0].children == '#[1]'

    * def states = result[0].children[0]
    * match states.children != '#[0]'
    * match states.name == 'NEW SOUTH WALES'
    # Ensure that response has city values
    * def cities = states.children
    * match cities[0].name == 'SYDNEY'
    * match cities[0].children == '#[1]'

