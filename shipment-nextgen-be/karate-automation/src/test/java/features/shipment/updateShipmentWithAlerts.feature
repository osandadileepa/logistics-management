Feature: Update a Shipment Feature

  Background:
    * url baseUrl
    * def bearer = token

    * def createShipmentResult = callonce read('classpath:features/shipment/addSingleWithAlerts.feature')
    * def shipmentTrackingIdToUpdate = createShipmentResult.response.data.id

    * def updateRequestPath = karate.get('singleRQPath', 'classpath:features/shipment/json/single/shipment-with-alerts.json')
    * def updateRequest = read(updateRequestPath)
    * updateRequest.data.id = shipmentTrackingIdToUpdate
    * updateRequest.data.shipment_tracking_id = createShipmentResult.data.shipment_tracking_id
    * updateRequest.data.order.id = createShipmentResult.data.order.id
    * updateRequest.data.shipment_package.id = createShipmentResult.response.data.shipment_package.id
    * updateRequest.data.shipment_journey.journey_id = createShipmentResult.response.data.shipment_journey.journey_id

  @ShipmentUpdate @Regression @ShipmentWithAlert
  Scenario: Update Shipment
    Given path '/shipments'
    And header Authorization = 'Bearer '+ bearer
    And request updateRequest
    When method PUT
    Then status 200

    * def response = $
    * print response
    * def data = response.data

    * assert data.shipment_journey.alerts.length >= 1
    * match data.shipment_journey.alerts[0].message == '#notnull'
    * match data.shipment_journey.alerts[0].type == '#notnull'
    * match data.shipment_journey.alerts[0].constraint == '#notnull'
    * match data.shipment_journey.alerts[0].dismissed == false


    * assert data.shipment_journey.package_journey_segments.length >= 3
    * match data.shipment_journey.package_journey_segments[0].alerts[0].message == '#notnull'
    * match data.shipment_journey.package_journey_segments[0].alerts[0].type == '#notnull'
    * match data.shipment_journey.package_journey_segments[0].alerts[0].dismissed == false
    * match data.shipment_journey.package_journey_segments[1].alerts[0].message == '#notnull'
    * match data.shipment_journey.package_journey_segments[1].alerts[0].type == '#notnull'
    * match data.shipment_journey.package_journey_segments[1].alerts[0].dismissed == false
    * match data.shipment_journey.package_journey_segments[2].alerts[0].message == '#notnull'
    * match data.shipment_journey.package_journey_segments[2].alerts[0].type == '#notnull'
    * match data.shipment_journey.package_journey_segments[2].alerts[0].dismissed == false