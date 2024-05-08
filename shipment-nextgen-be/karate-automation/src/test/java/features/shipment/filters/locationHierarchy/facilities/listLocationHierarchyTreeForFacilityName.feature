Feature: List of Location Hierarchy Trees With Facility Nodes

  Background:
    * url baseUrl
    * def bearer = token
    * def listStatesResult = callonce read('classpath:features/shipment/filters/listCities.feature')
    * print listStatesResult
    * def organizationId = listStatesResult.response.data.result[0].organization_id
    * print organizationId
    * def facilityName = 'NINOY'
    * print organizationId

  @GetLocationHierarchyTree  @GetLocationHierarchyTreeFacility @Regression
  Scenario: List of Location Hierarchy Trees With Facility Nodes Given Facility Name
    * def bearer = token
    Given url baseUrl + '/filter/location_hierarchies?key=' + facilityName + '&per_page=10&page=1&level=4'
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
    * match data.total_pages == 1

    * match result != '#[0]'

    # Ensure that response has country values
    * def country = result[0]
    * match country.children == '#[1]'
    * match country.name == 'PHILIPPINES'
    * match country.type == 'COUNTRY'

    # Ensure that response has state values
    * def states = country.children[0]
    * match states.children != '#[0]'
    * match states.name == 'METRO MANILA'
    * match states.type == 'STATE'

    # Ensure that response has city values
    * def cities = states.children[0]
    * match cities.children != '#[0]'
    * match cities.name == 'PASAY'
    * match cities.type == 'CITY'

    # Ensure that response has facility values
    * def facilities = cities.children[0]
    * match facilities.name == 'NINOY AQUINO INTERNATIONAL AIRPORT'
    * match facilities.type == 'FACILITY'

