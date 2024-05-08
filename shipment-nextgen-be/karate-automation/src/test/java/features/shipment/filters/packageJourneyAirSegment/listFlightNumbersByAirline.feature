Feature: List All Flight Numbers by Airline Feature

  Background:
    * def utils = Java.type('com.quincus.karate.automation.utils.DataUtils')
    * url baseUrl
    * def bearer = token
    * def requestPath = karate.get('singleRQPath', 'classpath:features/shipment/json/single/shipment-with-segment.json')
    * def updatedCreateRequest = read(requestPath)
    * print updatedCreateRequest
    * def airline = updatedCreateRequest.data.shipment_journey.package_journey_segments[2].airline

  @ListFlightNumbersByAirline @Regression
  Scenario: List All Flight Numbers by Airline
    * def bearer = token
    * def decodedUrl = utils.decodeUrl(baseUrl + '/package-journey-air-segment/filter/airlines/' + airline + '?per_page=10&page=1')
    Given url decodedUrl
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * print response

    * match response.data.result != '#[0]'
    * match response.data.result[0].id == '#present'
    * match response.data.result[0].id == '#notnull'
    * match response.data.result[0].name == '#present'
    * match response.data.result[0].name == '#notnull'

  @ListFlightNumbersByAirline @Regression
  Scenario: Return empty response with unknown airline
    * def bearer = token
    * def unknownAirline = 'unknown'
    * def decodedUrl = utils.decodeUrl(baseUrl + '/package-journey-air-segment/filter/airlines/' + unknownAirline + '?per_page=10&page=1')
    Given url decodedUrl
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * print response

    * assert response.data.total_elements == 0