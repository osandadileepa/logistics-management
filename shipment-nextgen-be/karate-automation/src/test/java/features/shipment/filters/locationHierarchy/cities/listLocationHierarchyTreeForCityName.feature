Feature: List of Location Hierarchy Trees With City Nodes

  Background:
    * def utils = Java.type('com.quincus.karate.automation.utils.DataUtils')
    * url baseUrl
    * def bearer = token
    * def listStatesResult = callonce read('classpath:features/shipment/filters/listStates.feature')
    * print listStatesResult
    * def organizationId = listStatesResult.response.data.result[0].organization_id
    * print organizationId
    * def cityName = 'SEATTLE'
    * print cityName

  @GetLocationHierarchyTree  @GetLocationHierarchyTreeCity @Regression
  Scenario: List of Location Hierarchy Trees With City Nodes Given City Name
    * def bearer = token
    Given url baseUrl + '/filter/location_hierarchies?key=' + cityName + '&per_page=10&page=1&level=3'
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
    # Ensure that they are in ascending order based on country name
    * match result[0].type == 'COUNTRY'

     # Ensure that response has state values
    * match result[0].children == '#[1]'

    * def states = result[0].children[0]
    * match states.children != '#[0]'
    * match utils.toUpperCase(states.name) == 'WASHINGTON'

    # Ensure that response has city values
    * def cities = states.children
    * match utils.toUpperCase(cities[0].name) == 'SEATTLE'
    * match cities[0].children == '#[0]'
