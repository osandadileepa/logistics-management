Feature: Simulate Order Message with 50 shipments from Order Module

  Background:
    * url baseUrl
    * def bearer = token

  @SimulateOrderMessageWith50ShipmentsFromOrderModule
  @Utility
  #30-33 secs for 50 shipments, this is on a local bench mark, so it might be faster on higher environment
  Scenario: Simulate Order with 50 Shipments Message from Order Module

    * def orderMessage = read('classpath:features/utility/json/orderMessageWith50Shipments.json')
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

    * utils.sleep(55000)

    * def shipment_tracking_id = orderMessage.shipments[0].shipment_id_label

    Given url baseUrl + '/shipments?shipment_tracking_id='+ shipment_tracking_id
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * def data = response.data
    * print data
    * match data.shipment_tracking_id == shipment_tracking_id
    * match data.milestone == '#notnull'
