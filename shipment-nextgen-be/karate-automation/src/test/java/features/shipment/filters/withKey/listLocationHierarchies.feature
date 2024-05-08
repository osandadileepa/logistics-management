Feature: List All Location Hierarchies

  Background:
    * url baseUrl
    * def bearer = token
    * def jsonPath = 'classpath:features/shipment/json/bulk/addBulkRQ.json'
    * def createBulkShipmentResult = callonce read('classpath:features/shipment/addBulk.feature') {bulkRQPath : '#(jsonPath)'}
    * def countryName = karate.get('loc_key', createBulkShipmentResult.data[0].shipment.origin.country_name)
    * print countryName

  @ListLocationHierarchies @Regression
  Scenario: List All Location Hierarchies with Key
    * def bearer = token
    Given url baseUrl + '/filter/location_hierarchies?key=' + countryName + '&per_page=10&page=1&level=1'
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * print response

    * match response.data.result != '#[0]'