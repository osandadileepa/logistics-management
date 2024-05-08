Feature: Create a Single Shipment with Same Shipment Tracking Id Feature

  Background:
    * url baseUrl
    * def createShipmentResult = callonce read('classpath:features/shipment/addSingle.feature')
    * def requestPath = karate.get('singleRQPath', 'classpath:features/shipment/json/single/addSingleRQ.json')
    * def updatedCreateRequest = read(requestPath)
    * def created_tracking_id = createShipmentResult.updatedCreateRequest.data.shipment_tracking_id
    * updatedCreateRequest.data.order.id = utils.uuid()
    * updatedCreateRequest.data.order.order_id_label = utils.uuid().substring(0, 10)
    * updatedCreateRequest.data.shipment_tracking_id = created_tracking_id
    * print updatedCreateRequest

  @ShipmentCreate
  @Regression
  Scenario: Create a Shipment with different OrderId but same ShipmentTrackingId
    * def bearer = token
    Given path '/shipments'
    And header Authorization = 'Bearer '+ bearer
    And request updatedCreateRequest
    When method POST
    Then status 400

