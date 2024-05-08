Feature: Create Shipments from OM Payload Feature

  Background:
    * url baseUrl
    * def requestPath = karate.get('singleRQPath', 'classpath:features/shipment/json/omPayload.json')
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
  Scenario: Create Shipments from OM Payload
    * def bearer = token
    Given path '/orders'
    And header Authorization = 'Bearer '+ bearer
    And request updatedRequest
    When method POST
    Then status 200

    * def response = $

    * print response
    * def data = response.data

    * match data == '#present'
    * match data == '#notnull'
    * match data == '##array'
    * match data == '#[2]'

    * match response.data[0].shipment_journey == '#present'
    * match response.data[0].shipment_journey == '#notnull'
    * match response.data[0].shipment_journey.journey_id == '#present'
    * match response.data[0].shipment_journey.journey_id == '#notnull'
    * def journeyId = response.data[0].shipment_journey.journey_id

    * match response.data[1].shipment_journey == '#notnull'
    * match response.data[1].shipment_journey == '#present'
    * match response.data[1].shipment_journey.journey_id == '#present'
    * match response.data[1].shipment_journey.journey_id == '#notnull'
    * match response.data[1].shipment_journey.journey_id == journeyId
