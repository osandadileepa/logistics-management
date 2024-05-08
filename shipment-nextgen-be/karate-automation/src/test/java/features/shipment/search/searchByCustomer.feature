Feature: Search by Customer Feature

  Background:
    * url baseUrl
    * def bearer = token
    * def jsonPath = 'classpath:features/shipment/json/bulk/addBulkRQ.json'
    * def createBulkShipmentResult = callonce read('classpath:features/shipment/addBulk.feature') {bulkRQPath : '#(jsonPath)'}

    * def bearer = token
    Given url baseUrl + '/filter/customers?per_page=10&page=1'
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200
    * def response = $

    * def customerId = response.data.result[0].id
    * def findAllRequest = read('classpath:features/shipment/json/search/customerRQ.json')
    * findAllRequest.data.customer[0].id = customerId
    * print findAllRequest

  @ShipmentFindAll @Regression
  Scenario: Find All Shipments
    * def bearer = token
    Given url baseUrl + '/shipments/list'
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
    * match data.result == '#[1]'
    * match data.filter.customer[0].id == customerId
