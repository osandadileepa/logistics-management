Feature: List All Facilities Feature

  Background:
    * url baseUrl
    * def bearer = token
    * def jsonPath = 'classpath:features/shipment/json/bulk/addBulkRQ.json'
    * def createBulkShipmentResult = callonce read('classpath:features/shipment/addBulk.feature') {bulkRQPath : '#(jsonPath)'}
    * def startFacility = createBulkShipmentResult.data[0].shipment.shipment_journey.package_journey_segments[0].start_facility
    * def facilityId = startFacility.id
    * print facilityId

  @ListFacilities @Regression
  Scenario: List All Facilities sort by name
    * def bearer = token
    Given url baseUrl + '/filter/locations?type=FACILITY&per_page=10&page=1&sort_by=name'
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * print response

    * match response.data.result != '#[0]'
    * match response.data.result[0].type == 'FACILITY'

    * match response.data.result contains deep { name : 'AUSTRALIA_NEWSOUTHWALES_SYDNEY_FAC1' }

  @ListFacilities @Regression
  Scenario: List All Facilities sort by code
    * def bearer = token
    Given url baseUrl + '/filter/locations?type=FACILITY&per_page=10&page=1&sort_by=code'
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * print response

    * match response.data.result != '#[0]'
    * match response.data.result[0].type == 'FACILITY'

