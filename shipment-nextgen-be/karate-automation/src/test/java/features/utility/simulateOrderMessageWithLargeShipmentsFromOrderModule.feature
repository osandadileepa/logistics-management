#This feature is not intended to be part of the automated Karate test suite due to potential delays. It should be executed manually when needed.
Feature: Simulate Order Message with Large shipments from Order Module

  Background:
    * url baseUrl
    * def bearer = token

  @SimulateOrderMessageWith500ShipmentsFromOrderModule
  @Utility
  @ignore #enable this to run this method
  Scenario: Simulate Order Message with 500 Shipments from Order Module

    * def orderMessage = read('classpath:features/utility/json/orderMessageWith500Shipments.json')
    * def topicName = 'local-ext-ordermodule-orders_shpv2'
    * def orderIdLabel = utils.uuid().substring(0, 10)
    * orderMessage.order_id_label = orderIdLabel
    * orderMessage.id = utils.uuid()

    * string orderMessageString = orderMessage
    * def modifiedOrderMessageString = utils.modifyShipmentIdLabels(orderMessageString)
    * json orderMessage = modifiedOrderMessageString

    Given path '/kafka/producers/' + topicName
    And header Authorization = 'Bearer '+ bearer
    And request orderMessage
    When method POST
    Then status 200

    * utils.sleep(360000)

    * def shipment_tracking_id = orderMessage.shipments[0].shipment_id_label

    Given url baseUrl + '/shipments?shipment_tracking_id='+ shipment_tracking_id
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * def data = response.data
    * print data
    * match data.shipment_tracking_id == shipment_tracking_id
    * match data.description == '#notnull'