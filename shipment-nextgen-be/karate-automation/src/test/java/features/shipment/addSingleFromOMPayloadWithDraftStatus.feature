Feature: Create Shipments from OM Payload With Draft Status Feature

  Background:
    * url baseUrl
    * def requestPath = karate.get('singleRQPath', 'classpath:features/shipment/json/omPayloadWithDraftStatus.json')
    * def updatedRequest = read(requestPath)
    * def orderId = utils.uuid()
    * def shipmentCode = utils.uuid().substring(0, 16)

    * updatedRequest.data.id = orderId
    * updatedRequest.data.shipment_code = shipmentCode
    * updatedRequest.data.order_id_label = utils.uuid().substring(0, 10)

    * updatedRequest.data.shipments[0].order_id = orderId
    * updatedRequest.data.shipments[0].shipment_id_label = shipmentCode + '-001'

    * updatedRequest.data.shipments[1].order_id = orderId
    * updatedRequest.data.shipments[1].shipment_id_label = shipmentCode + '-002'

  @ShipmentCreateFromOMPayload
  @Regression
  Scenario: Create Shipments from OM Payload With Draft Status
    * def bearer = token
    Given path '/orders'
    And header Authorization = 'Bearer '+ bearer
    And request updatedRequest
    When method POST
    Then status 400

    * def response = $

    * print response
    * def data = response.message
    * print data

    * match data == '#present'
    * match data == '#notnull'
    * match data contains 'The requested operation cannot be performed on a draft entity'
