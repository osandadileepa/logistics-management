Feature: Create Bulk Shipment Feature

  Background:
    * url baseUrl
    * def requestPath = karate.get('bulkRQPath', 'classpath:features/shipment/json/bulk/addBulkRQ.json')
    * def updatedBulkRequest = read(requestPath)
    * def orderId1 = "One_" + utils.uuid().substring(0, 10)
    * def orderId2 = "Two_" + utils.uuid().substring(0, 10)
    * def orderIdLabel1 = "OIDL1_" + utils.uuid().substring(0, 10)
    * def orderIdLabel2 = "OIDL2_" + utils.uuid().substring(0, 10)
    * updatedBulkRequest.data[0].shipment_tracking_id = 'SHP' + utils.uuid().substring(0, 12)
    * updatedBulkRequest.data[1].shipment_tracking_id = 'SHP' + utils.uuid().substring(0, 12)
    * updatedBulkRequest.data[0].order.id = orderId1
    * updatedBulkRequest.data[1].order.id = orderId2
    * updatedBulkRequest.data[0].order.order_id_label = orderIdLabel1
    * updatedBulkRequest.data[1].order.order_id_label = orderIdLabel2
    * print updatedBulkRequest

  @ShipmentCreateBulk
  @Regression
  Scenario: Create Bulk Shipment
    * def bearer = token
    * def shipmentTrackingID = utils.uuid()
    Given path '/shipments/bulk'
    And header Authorization = 'Bearer '+ bearer
    And request updatedBulkRequest
    When method POST
    Then status 200

    * def response = $
    * print response

    * def data = response.data
    * def shipment1 = data[0].shipment
    * def shipment2 = data[1].shipment
    * def status1 = data[0].success
    * def status2 = data[1].success

    * match shipment1.shipment_tracking_id == '#present'
    * match shipment1.shipment_tracking_id == '#notnull'
    * match status1 == true
    * match shipment2.shipment_tracking_id == '#present'
    * match shipment2.shipment_tracking_id == '#notnull'
    * match status2 == true

    * def startFacilityName1 = updatedBulkRequest.data[0].shipment_journey.package_journey_segments[0].start_facility.name
    * def startFacilityCode1 = updatedBulkRequest.data[0].shipment_journey.package_journey_segments[0].start_facility.code

    * match shipment1.shipment_journey.package_journey_segments[0].start_facility.name == startFacilityName1
    * match shipment1.shipment_journey.package_journey_segments[0].start_facility.code == startFacilityCode1

    * def startFacilityName2 = updatedBulkRequest.data[1].shipment_journey.package_journey_segments[0].start_facility.name
    * def startFacilityCode2 = updatedBulkRequest.data[1].shipment_journey.package_journey_segments[0].start_facility.code

    * match shipment2.shipment_journey.package_journey_segments[0].start_facility.name == startFacilityName2
    * match shipment2.shipment_journey.package_journey_segments[0].start_facility.code == startFacilityCode2

