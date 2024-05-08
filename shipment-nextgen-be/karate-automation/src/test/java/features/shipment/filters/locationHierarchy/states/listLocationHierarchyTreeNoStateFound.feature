Feature: List of Location Hierarchy Tress with State Nodes

  Background:
    * url baseUrl
    * def bearer = token
    * def listCountriesResult = callonce read('classpath:features/shipment/filters/listCountries.feature')
    * def organizationId = listCountriesResult.response.data.result[0].organization_id
    * print organizationId

  @GetLocationHierarchyTree  @GetLocationHierarchyTreeState @Regression
  Scenario: Should have empty result when an invalid key is passed
    * def bearer = token
    Given url baseUrl + '/filter/location_hierarchies?per_page=10&page=1&leve2=1&key=ANonexistentCountry'
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
