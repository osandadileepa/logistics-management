Feature: List Location Hierarchy Trees With Country Nodes and Filter Page Out of Bound

  Background:
    * url baseUrl
    * def bearer = token
    * def listLocationHierarchyTreeCountriesResult = callonce read('classpath:features/shipment/filters/locationHierarchy/countries/listLocationHierarchyTreeCountries.feature')
    * def totalPageNumber = listLocationHierarchyTreeCountriesResult.data.total_pages
    * def outOfBoundPageNumber = totalPageNumber + 10000


  @GetLocationHierarchyTree  @GetLocationHierarchyTreeCountry @Regression
  Scenario: List All Location Hierarchy Trees With Country Nodes Given Page Number Out of Bound
    * def bearer = token
    Given url baseUrl + '/filter/location_hierarchies?level=1&per_page=10&page=' + outOfBoundPageNumber
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