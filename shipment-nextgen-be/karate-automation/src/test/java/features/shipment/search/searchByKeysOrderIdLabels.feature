Feature: Search by Order Id Labels Keys Feature

  Background:
    * def utils = Java.type('com.quincus.karate.automation.utils.DataUtils')
    * url baseUrl
    * def bearer = token
    * def createShipmentResult = callonce read('classpath:features/shipment/addBulk.feature')
    * def findAllRequest = read('classpath:features/shipment/json/search/keysOrderIdLabelsRQ.json')
    * findAllRequest.data.keys[0] = createShipmentResult.updatedBulkRequest.data[0].order.order_id_label
    * findAllRequest.data.keys[1] = createShipmentResult.updatedBulkRequest.data[1].order.order_id_label
    * print findAllRequest

  @ShipmentFindAll @Regression
  Scenario: Find All Shipments with keys order id labels
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
    * match data.result == '#[1]'
    * match data.filter.keys[0] == req.keys[0]
    * match data.filter.keys[1] == req.keys[1]

  @ShipmentFindAll @Regression
  Scenario: Find All Shipments with keys lower case order id labels
    * def bearer = token
    * findAllRequest.data.keys[0] = utils.toLowerCase(findAllRequest.data.keys[0])
    * findAllRequest.data.keys[1] = utils.toLowerCase(findAllRequest.data.keys[1])
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
    * match data.result == '#[1]'
    * match data.filter.keys[0] == req.keys[0]
    * match data.filter.keys[1] == req.keys[1]

  @ShipmentFindAll @Regression
  Scenario: Find All Shipments with keys upper case order id labels
    * def bearer = token
    * findAllRequest.data.keys[0] = utils.toUpperCase(findAllRequest.data.keys[0])
    * findAllRequest.data.keys[1] = utils.toUpperCase(findAllRequest.data.keys[1])
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
    * match data.result == '#[1]'
    * match data.filter.keys[0] == req.keys[0]
    * match data.filter.keys[1] == req.keys[1]

  @ShipmentFindAll
  Scenario: Find All Shipments with keys unknown order id labels
    * def bearer = token
    * findAllRequest.data.keys[0] = 'unknown_order_id_1'
    * findAllRequest.data.keys[1] = 'unknown_order_id_2'
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
