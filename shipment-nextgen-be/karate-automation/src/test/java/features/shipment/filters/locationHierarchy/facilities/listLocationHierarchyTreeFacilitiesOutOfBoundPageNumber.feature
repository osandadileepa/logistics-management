Feature: List of Location Hierarchy Trees With Facility Nodes and Filter Page Out of Bound

  Background:
    * url baseUrl
    * def bearer = token
    * def listLocationHierarchyTreeFacilitiesResult = callonce read('classpath:features/shipment/filters/locationHierarchy/facilities/listLocationHierarchyTreeFacilities.feature')
    * def totalPageNumber = listLocationHierarchyTreeFacilitiesResult.data.total_pages
    * def outOfBoundPageNumber = totalPageNumber + 10000

  @GetLocationHierarchyTree  @GetLocationHierarchyTreeFacility @Regression
  Scenario: List Location Hierarchies Trees With Facility Nodes Given Page Number Out of Bound
    * def bearer = token
    Given url baseUrl + '/filter/location_hierarchies?level=4&per_page=10&page=' + outOfBoundPageNumber
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * print response

    * def result = response.data.result
    * def data = response.data

    # Check if data has valid count and result should be blank on out of bound page number
    * match data.total_elements != '#[0]'
    * assert data.total_pages >= totalPageNumber
    * match result == '#[0]'