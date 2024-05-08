Feature: Simulate Order Message from Order Module

  Background:
    * url baseUrl
    * def bearer = token

  @SimulateOrderMessageFromOrderModule
  @Regression
  Scenario: Simulate Order Message from Order Module

    * def orderMessage = read('classpath:features/utility/json/orderMessage.json')
    * def topicName = 'local-ext-ordermodule-orders_shpv2'
    * def shipment_tracking_id = 'SHP' + utils.uuid().substring(0, 12)
    * def orderIdLabel = utils.uuid().substring(0, 10)
    * orderMessage.shipments[0].shipment_id_label = shipment_tracking_id
    * orderMessage.order_id_label = orderIdLabel
    * orderMessage.id = utils.uuid()

    Given path '/kafka/producers/' + topicName
    And header Authorization = 'Bearer '+ bearer
    And request orderMessage
    When method POST
    Then status 200

    * utils.sleep(5000)

    Given url baseUrl + '/shipments?shipment_tracking_id='+ shipment_tracking_id
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * def data = response.data
    * print data
    * match data.shipment_tracking_id == shipment_tracking_id
    * match data.description == '#notnull'