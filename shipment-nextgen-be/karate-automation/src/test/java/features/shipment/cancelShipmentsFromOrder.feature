Feature: Cancel Shipment From an Order Feature

  Background:
    * url baseUrl
    * def bearer = token
    * def createShipmentResult = callonce read('classpath:features/shipment/addSingleFromOMPayloadWithSameShipmentJourney.feature')
    * def shipment_id_1 = createShipmentResult.data[0].id
    * def shipment_id_2 = createShipmentResult.data[1].id

  @ShipmentCancelFromOrder @Regression
  Scenario: Cancel Journey when All Shipments from an Order is Cancelled
    Given path '/shipments/cancel/' + shipment_id_1
    And header Authorization = 'Bearer '+ bearer
    When method PATCH
    Then status 200

    * def response = $
    * print response
    * match response.status == 'Shipment Cancelled.'
    * match response.data.shipment_journey.status != 'CANCELLED'

    Given path '/shipments/cancel/' + shipment_id_2
    And header Authorization = 'Bearer '+ bearer
    When method PATCH
    Then status 200

    * def response = $
    * print response
    * match response.status == 'Shipment Cancelled.'
    * match response.data.shipment_journey.status == 'CANCELLED'