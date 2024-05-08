Feature: Search by Order Label, Shipment Tracking Id, Waybills, Flight Numbers, External Order Id, Internal Order Id, Additional Tracking Number and Customer Order Id  Keys,

  Background:
    * def utils = Java.type('com.quincus.karate.automation.utils.DataUtils')
    * url baseUrl
    * def bearer = token
    * def createShipmentResult = callonce read('classpath:features/shipment/addBulk.feature')
    * def findAllRequest = read('classpath:features/shipment/json/search/keysRQ.json')
    * findAllRequest.data.keys[0] = createShipmentResult.updatedBulkRequest.data[0].order.order_id_label
    * findAllRequest.data.keys[1] = createShipmentResult.updatedBulkRequest.data[0].shipment_tracking_id
    * findAllRequest.data.keys[2] = createShipmentResult.updatedBulkRequest.data[1].order.order_id_label
    * findAllRequest.data.keys[3] = createShipmentResult.updatedBulkRequest.data[1].shipment_tracking_id
    * findAllRequest.data.keys[4] = createShipmentResult.updatedBulkRequest.data[0].shipment_journey.package_journey_segments[2].flight_number
    * findAllRequest.data.keys[5] = createShipmentResult.updatedBulkRequest.data[0].shipment_journey.package_journey_segments[2].master_waybill
    * print findAllRequest

  @ShipmentFindAll @Regression
  Scenario: Find All Shipments with Keys Order Id label, Tracking Ids, Waybills, and Flight Numbers
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
    * match data.filter.keys[2] == req.keys[2]
    * match data.filter.keys[3] == req.keys[3]
    * match data.filter.keys[4] == req.keys[4]
    * match data.filter.keys[5] == req.keys[5]

  @ShipmentFindAll @Regression
  Scenario: Find All Shipments with only one existing key
    * def bearer = token
    * findAllRequest.data.keys[0] = utils.toUpperCase(findAllRequest.data.keys[0])
    * findAllRequest.data.keys[1] = 'unknown'
    * findAllRequest.data.keys[2] = 'unknown'
    * findAllRequest.data.keys[3] = 'unknown'
    * findAllRequest.data.keys[4] = 'unknown'
    * findAllRequest.data.keys[5] = 'unknown'
    Given path '/shipments/list'
    And header Authorization = 'Bearer '+ bearer
    And request findAllRequest
    When method POST
    Then status 200

    * def response = $
    * print response

    * def req = findAllRequest.data
    * def data = response.data

    * assert data.total_elements == 1
    * assert data.total_pages >= 1
    * match data.filter.size == req.size
    * match data.filter.page_number == req.page_number
    * match data.current_page == req.page_number
    * match data.result == '#[1]'
    * match data.filter.keys[0] == req.keys[0]
    * match data.filter.keys[1] == req.keys[1]
    * match data.filter.keys[2] == req.keys[2]
    * match data.filter.keys[3] == req.keys[3]
    * match data.filter.keys[4] == req.keys[4]
    * match data.filter.keys[5] == req.keys[5]
