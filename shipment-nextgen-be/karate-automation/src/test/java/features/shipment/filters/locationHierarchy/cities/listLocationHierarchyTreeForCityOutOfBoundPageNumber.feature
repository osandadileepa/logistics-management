Feature: List of Location Hierarchy Trees With City Nodes and Filter Page Out of Bound

  Background:
    * url baseUrl
    * def bearer = token
    * def listLocationHierarchyTreeCitiesResult = callonce read('classpath:features/shipment/filters/locationHierarchy/cities/listLocationHierarchyTreeCities.feature')
    * def cityName = 'SEATTLE'
    * def totalPageNumber = listLocationHierarchyTreeCitiesResult.data.total_pages
    * def outOfBoundPageNumber = totalPageNumber + 10000

  @GetLocationHierarchyTree  @GetLocationHierarchyTreeCity @Regression
  Scenario: List of Location Hierarchy Trees With City Nodes Given City Name and Page Filter Out Of Bound
    * def bearer = token
    Given url baseUrl + '/filter/location_hierarchies?key=' + cityName + '&level=3&per_page=10&page=' + outOfBoundPageNumber
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

