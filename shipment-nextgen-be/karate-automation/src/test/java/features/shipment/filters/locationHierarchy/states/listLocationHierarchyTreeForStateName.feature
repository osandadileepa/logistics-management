Feature: List of Location Hierarchy Trees With State Nodes

  Background:
    * url baseUrl
    * def bearer = token
    * def listStatesResult = callonce read('classpath:features/shipment/filters/listStates.feature')
    * print listStatesResult
    * def organizationId = listStatesResult.response.data.result[0].organization_id
    * print organizationId
    * def stateName = 'CENTRAL'
    * print organizationId
    * print stateName

  @GetLocationHierarchyTree  @GetLocationHierarchyTreeState @Regression
  Scenario: List of Location Hierarchy Trees With State Nodes Given State Name
    * def bearer = token
    Given url baseUrl + '/filter/location_hierarchies?key=' + stateName + '&per_page=10&page=1&level=2'
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
     # Ensure that response has state values
    * match result[0].children == '#[1]'

    * def states = result[0].children[0]
    * match states.children == '#[0]'
    * def statesName = states.name
    * match statesName.toUpperCase() == 'CENTRAL SINGAPORE COMMUNITY DEVELOPMENT COUNCIL'