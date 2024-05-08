Feature: List All Countries Feature

  Background:
    * url baseUrl
    * def bearer = token
    * def shipmentDetails = callonce read('classpath:features/shipment/get.feature')
    * def shipment = shipmentDetails.response.data
    * def startFacility = shipment.shipment_journey.package_journey_segments[0].start_facility
    * def countryLhId = startFacility.location.country
    * print countryLhId
    * def stateLhId = startFacility.location.state
    * print stateLhId
    * def cityLhId = startFacility.location.city
    * print cityLhId
    * def facilityLhId = startFacility.id
    * print facilityLhId

  @Cascader @Regression
  Scenario: Cascader
    * def bearer = token
    Given url baseUrl + '/filter/locations?type=COUNTRY&per_page=10&page=1'
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * print response

    * match response.data.result != '#[0]'
    * match each response.data.result contains { type : 'COUNTRY' }
    * match response.data.result contains deep { id : #(countryLhId) }

    * def bearer = token
    Given url baseUrl + '/filter/states?country_id=' +countryLhId+ '&per_page=10&page=1'
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * print response

    * match response.data.result != '#[0]'
    * match each response.data.result contains { type : 'STATE' }
    * match response.data.result contains deep { id : '#(stateLhId)' }

    * def bearer = token
    Given url baseUrl + '/filter/cities?country_id=' +countryLhId+ '&per_page=10&page=1&state_id=' + stateLhId
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * print response

    * match response.data.result != '#[0]'
    * match each response.data.result contains { type : 'CITY' }
    * match response.data.result contains deep { id : '#(cityLhId)' }

    * def bearer = token
    Given url baseUrl + '/filter/facilities?country_id=' +countryLhId+ '&per_page=10&page=1&state_id=' + stateLhId + '&city_id=' + cityLhId
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * print response

    * match response.data.result != '#[0]'
    * match each response.data.result contains { type : 'FACILITY' }
    * match response.data.result contains deep { id : '#(facilityLhId)' }
    * match response.data.result contains deep { code : 'SYDF1' }