Feature: Search by Booking Date Feature

  Background:
    * url baseUrl
    * def bearer = token
    * def jsonPath = 'classpath:features/shipment/json/bulk/addBulkRQ.json'
    * def createBulkShipmentResult = callonce read('classpath:features/shipment/addBulk.feature') {bulkRQPath : '#(jsonPath)'}
    * def findAllRequest = read('classpath:features/shipment/json/search/bookingDateRQ.json')
    * findAllRequest.data.booking_date_from = utils.getDateBefore()
    * findAllRequest.data.booking_date_to = utils.getFormattedDateNow()
    * print findAllRequest

  @ShipmentFindAllByBookingDate @Regression
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