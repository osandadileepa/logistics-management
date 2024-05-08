Feature: Update a Shipment Journey Feature

  Background:
    * url baseUrl
    * def bearer = token

    * def createShipmentsFromOrderResult = callonce read('classpath:features/shipment/addSingleFromOMPayload.feature')
    * def expectedShipmentId1 = createShipmentsFromOrderResult.data[0].id
    * print expectedShipmentId1
    * def expectedShipmentId2 = createShipmentsFromOrderResult.data[1].id
    * print expectedShipmentId2
    * def shipmentDetails1 = callonce read('classpath:features/shipment/get.feature') {existingShipmentId : '#(expectedShipmentId1)'}
    * def shipment1 = shipmentDetails1.response.data
    * print shipment1
    * def shipmentDetails2 = callonce read('classpath:features/shipment/get.feature') {existingShipmentId : '#(expectedShipmentId2)'}
    * def shipment2 = shipmentDetails2.response.data
    * print shipment2
    * def expectedShipmentTrackingIdUpdated1 = shipment1.shipment_tracking_id
    * def expectedShipmentTrackingIdUpdated2 = shipment2.shipment_tracking_id

    * def shipmentIdToUpdate = shipment1.id
    * def shipmentJourneyIdToUpdate = shipment1.shipment_journey.journey_id
    * def shipmentOrderIdToUpdate = shipment1.order.id
    * def requestForUpdate = read('classpath:features/shipment/json/updateJourneyRQ.json')
    * requestForUpdate.data.shipment_id = shipmentIdToUpdate
    * requestForUpdate.data.journey_id = shipmentJourneyIdToUpdate
    * requestForUpdate.data.order_id = shipmentOrderIdToUpdate
    * requestForUpdate.data.package_journey_segments[0].status = 'PLANNED'
    * requestForUpdate.data.package_journey_segments[2].ref_id = null
    * requestForUpdate.data.package_journey_segments[1].vehicle.type = 'CAR'
    * requestForUpdate.data.package_journey_segments[1].vehicle.name = 'CAR001'
    * requestForUpdate.data.package_journey_segments[1].vehicle.number = 'CAR 123'
    * requestForUpdate.data.package_journey_segments[1].driver.name = 'DRIVER'
    * requestForUpdate.data.package_journey_segments[1].driver.phone_code = '+62'
    * requestForUpdate.data.package_journey_segments[1].driver.phone_number = '000-11-22'


  @ShipmentJourneyUpdate @Regression
  Scenario: Update Shipment Journey & Other Related Shipment Journeys
    Given path '/shipments/shipment_journey'
    And header Authorization = 'Bearer '+ bearer
    And request requestForUpdate
    When method PUT
    Then status 200

    * def response = $
    * print response

    * match response.data.shipment_journey == '#present'
    * match response.data.shipment_journey == '#notnull'
    * match response.data.shipment_journey.journey_id == shipmentJourneyIdToUpdate
    * match response.data.shipment_journey.package_journey_segments[0].status == 'PLANNED'
    * match response.data.shipment_journey.package_journey_segments[1].vehicle.type == 'CAR'
    * match response.data.shipment_journey.package_journey_segments[1].vehicle.name == 'CAR001'
    * match response.data.shipment_journey.package_journey_segments[1].vehicle.number == 'CAR 123'
    * match response.data.shipment_journey.package_journey_segments[1].driver.name == 'DRIVER'
    * match response.data.shipment_journey.package_journey_segments[1].driver.phone_code == '+62'
    * match response.data.shipment_journey.package_journey_segments[1].driver.phone_number == '000-11-22'
    * match response.data.shipment_journey.package_journey_segments[2].ref_id == '3'
    * match response.data.updated_shipment_tracking_ids contains expectedShipmentTrackingIdUpdated1
    * match response.data.updated_shipment_tracking_ids contains expectedShipmentTrackingIdUpdated2
    * match response.data.total_shipments_updated == 2