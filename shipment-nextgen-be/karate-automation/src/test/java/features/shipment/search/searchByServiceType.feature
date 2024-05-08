Feature: Search by Service Type Feature

  Background:
    * url baseUrl
    * def bearer = token
    * def shipmentDetails = callonce read('classpath:features/shipment/get.feature')
    * def shipment = shipmentDetails.response.data
    * def serviceTypeId = shipment.service_type.id
    * def findAllRequest = read('classpath:features/shipment/json/search/serviceTypeRQ.json')
    * findAllRequest.data.service_type[0].id = serviceTypeId
    * print findAllRequest

  @ShipmentFindAll @Regression
  Scenario: Find All Shipments
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
    * match data.filter.service_type[0].id == serviceTypeId
