Feature: Update a Shipment Journey Feature

  Background:
    * url baseUrl
    * def bearer = token

    * def shipmentDetails = callonce read('classpath:features/shipment/get.feature')
    * def shipment = shipmentDetails.response.data
    * def shipmentId = shipment.id
    * def shipmentJourneyIdToUpdate = shipment.shipment_journey.journey_id
    * def shipmentTrackingId = shipment.shipment_tracking_id
    * def shipmentOrderId = shipment.order.id

    * def requestForUpdate = read('classpath:features/shipment/json/updateJourneyRQ.json')
    * requestForUpdate.data.shipment_id = shipmentId
    * requestForUpdate.data.journey_id = shipmentJourneyIdToUpdate
    * requestForUpdate.data.order_id = shipmentOrderId
    * requestForUpdate.data.package_journey_segments[0].status = 'PLANNED'
    * requestForUpdate.data.package_journey_segments[1].vehicle.type = 'CAR'
    * requestForUpdate.data.package_journey_segments[1].vehicle.name = 'CAR001'
    * requestForUpdate.data.package_journey_segments[1].vehicle.number = 'CAR 123'
    * requestForUpdate.data.package_journey_segments[1].driver.name = 'DRIVER'
    * requestForUpdate.data.package_journey_segments[1].driver.phone_code = '+62'
    * requestForUpdate.data.package_journey_segments[1].driver.phone_number = '000-11-22'
    * requestForUpdate.data.package_journey_segments[2].ref_id = null
    * requestForUpdate.data.package_journey_segments[2].sequence = null

    * print requestForUpdate

  @ShipmentJourneyUpdate @Regression
  Scenario: Update Shipment Journey
    Given path '/shipments/shipment_journey'
    And header Authorization = 'Bearer '+ bearer
    And request requestForUpdate
    When method PUT
    Then status 200

    * def response = $
    * match response.data.shipment_journey == '#present'
    * match response.data.shipment_journey == '#notnull'
    * match response.data.shipment_journey.journey_id == shipmentJourneyIdToUpdate
    * match response.data.shipment_journey.package_journey_segments[0].status == 'PLANNED'
    * match response.data.updated_shipment_tracking_ids contains shipmentTrackingId
    * match response.data.total_shipments_updated == 1
    * match response.data.shipment_journey.package_journey_segments[1].vehicle.type == 'CAR'
    * match response.data.shipment_journey.package_journey_segments[1].vehicle.name == 'CAR001'
    * match response.data.shipment_journey.package_journey_segments[1].vehicle.number == 'CAR 123'
    * match response.data.shipment_journey.package_journey_segments[1].driver.name == 'DRIVER'
    * match response.data.shipment_journey.package_journey_segments[1].driver.phone_code == '+62'
    * match response.data.shipment_journey.package_journey_segments[1].driver.phone_number == '000-11-22'
    * match response.data.shipment_journey.package_journey_segments[2].ref_id == '3'
    * match response.data.shipment_journey.package_journey_segments[2].sequence == '2'

    Given path '/shipments/' + shipmentId
    And header Authorization = 'Bearer ' + bearer
    When method GET
    Then status 200

    * def response = $
    * match response.data.shipment_journey.journey_id == shipmentJourneyIdToUpdate
    * match response.data.shipment_journey.package_journey_segments[0].status == 'PLANNED'
    * match response.data.shipment_tracking_id == shipmentTrackingId
    * match response.data.shipment_journey.package_journey_segments[1].vehicle.type == 'CAR'
    * match response.data.shipment_journey.package_journey_segments[1].vehicle.name == 'CAR001'
    * match response.data.shipment_journey.package_journey_segments[1].vehicle.number == 'CAR 123'
    * match response.data.shipment_journey.package_journey_segments[1].driver.name == 'DRIVER'
    * match response.data.shipment_journey.package_journey_segments[1].driver.phone_code == '+62'
    * match response.data.shipment_journey.package_journey_segments[1].driver.phone_number == '000-11-22'
    * match response.data.shipment_journey.package_journey_segments[2].ref_id == '3'
    * match response.data.shipment_journey.package_journey_segments[2].sequence == '2'