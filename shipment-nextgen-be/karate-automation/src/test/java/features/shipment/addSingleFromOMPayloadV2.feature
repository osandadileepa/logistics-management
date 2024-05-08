Feature: Create Shipments from OM Payload Feature V2

  Background:
    * url baseUrl
    * def requestPath = karate.get('singleRQPath', 'classpath:features/shipment/json/omPayload.json')
    * def updatedRequest = read(requestPath)
    * def orderId = utils.uuid()
    * def orderIdLabel = utils.uuid().substring(0, 16)
    * def shipmentCode = utils.uuid().substring(0, 16)
    * def shipmentTracking1 = shipmentCode + '-001'
    * def shipmentTracking2 = shipmentCode + '-002'


    * updatedRequest.data.id = orderId
    * updatedRequest.data.shipment_code = shipmentCode
    * updatedRequest.data.order_id_label = orderIdLabel

    * updatedRequest.data.shipments[0].order_id = orderId
    * updatedRequest.data.shipments[0].shipment_id_label = shipmentTracking1

    * updatedRequest.data.shipments[1].order_id = orderId
    * updatedRequest.data.shipments[1].shipment_id_label = shipmentTracking2

  @ShipmentCreateFromOMPayload
  @Regression
  Scenario: Create Shipments from OM Payload V2
    * def bearer = token
    Given path 'v2/orders'
    And header Authorization = 'Bearer '+ bearer
    And request updatedRequest
    When method POST
    Then status 200

    * def response = $

    * print response
    * def data = response.data

    * match data == '#present'
    * match data == '#notnull'
    * match data.order_id == orderId
    * match data.order_id_label == orderIdLabel
    * match data.shipment_tracking_ids[0] == shipmentTracking1
    * match data.shipment_tracking_ids[1] == shipmentTracking2

    * utils.sleep(20000)

    Given url baseUrl + '/shipments?shipment_tracking_id='+ shipmentTracking1
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * def data = response.data
    * print data
    * match data.order.id == orderId
    * match data.order.order_id_label == orderIdLabel
    * match data.shipment_tracking_id == shipmentTracking1
