Feature: Search by Alert Short Name

  Background:
    * url baseUrl
    * def bearer = token
    * def createShipmentResult = call read('classpath:features/shipment/addSingleWithAlerts.feature')
    * def findAllRequest = read('classpath:features/shipment/json/search/alertShortNameRQ.json')

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
    * match data.result[0].shipment_journey.alerts[0].short_message == "Blank mandatory field"