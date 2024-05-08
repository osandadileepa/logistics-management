Feature: Search by Airlines or Flight Numbers Feature

  Background:
    * def utils = Java.type('com.quincus.karate.automation.utils.DataUtils')
    * url baseUrl
    * def bearer = token
    * def createShipmentResult = callonce read('classpath:features/shipment/addSingleWithSegments.feature')
    * def findAllRequest = read('classpath:features/shipment/json/search/airlineKeysRQ.json')
    * print findAllRequest

  @ShipmentFindAll @Regression
  Scenario: Find All Shipments with airlines and flight numbers
    * def bearer = token
    Given path '/shipments/list'
    And header Authorization = 'Bearer '+ bearer
    And request findAllRequest
    When method POST
    Then status 200

    * def response = $
    * print response

    * def req = findAllRequest.data
    * def data = response.data

    * assert data.total_elements >= 1
    * assert data.total_pages >= 1
    * match data.filter.size == req.size
    * match data.filter.page_number == req.page_number
    * match data.current_page == req.page_number
    * match data.result != '#[0]'
    * match data.filter.airline_keys[0] == req.airline_keys[0]

  @ShipmentFindAll @Regression
  Scenario: Find All Shipments with airlines
    * findAllRequest.data.airline_keys[0].airline_name = 'Singapore Airlines'
    * findAllRequest.data.airline_keys[0].flight_numbers = []
    * def bearer = token
    Given path '/shipments/list'
    And header Authorization = 'Bearer '+ bearer
    And request findAllRequest
    When method POST
    Then status 200

    * def response = $
    * print response

    * def req = findAllRequest.data
    * def data = response.data

    * assert data.total_elements >= 1
    * assert data.total_pages >= 1
    * match data.filter.size == req.size
    * match data.filter.page_number == req.page_number
    * match data.current_page == req.page_number

  @ShipmentFindAll @Regression
  Scenario: Find All Shipments with flight numbers
    * findAllRequest.data.airline_keys[0].airline_name = 'Singapore Airlines'
    * findAllRequest.data.airline_keys[0].flight_numbers = ['AR1324']
    * def bearer = token
    Given path '/shipments/list'
    And header Authorization = 'Bearer '+ bearer
    And request findAllRequest
    When method POST
    Then status 200

    * def response = $
    * print response

    * def req = findAllRequest.data
    * def data = response.data

    * assert data.total_elements >= 1
    * assert data.total_pages >= 1
    * match data.filter.size == req.size
    * match data.filter.page_number == req.page_number
    * match data.current_page == req.page_number

  @ShipmentFindAll @Regression
  Scenario: Find All Shipments with unknown Airline and  numbers
    * def bearer = token
    * findAllRequest.data.airline_keys[0].airline_name = 'Unknown Airlines'
    * findAllRequest.data.airline_keys[0].flight_numbers = ['Unknown Flight Number']
    Given path '/shipments/list'
    And header Authorization = 'Bearer '+ bearer
    And request findAllRequest
    When method POST
    Then status 200

    * def response = $
    * print response

    * def req = findAllRequest.data
    * def data = response.data

    * assert data.total_elements == 0
    * assert data.total_pages == 0