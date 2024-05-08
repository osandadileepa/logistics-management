Feature: Create Shipment from OM Payload Feature Then Update Journey and add another shipment

  Background:
    * url baseUrl
    * def bearer = token

    * def requestPath = karate.get('singleRQPath', 'classpath:features/shipment/json/omPayloadSingleShipmentNoSegment.json')
    * def initialRequest = read(requestPath)
    * def orderId = utils.uuid()
    * def shipmentCode = utils.uuid().substring(0, 16)
    * initialRequest.data.id = orderId
    * initialRequest.data.external_order_id = utils.uuid()
    * initialRequest.data.shipment_code = shipmentCode
    * initialRequest.data.order_id_label = utils.uuid().substring(0, 10)
    * initialRequest.data.shipments[0].order_id = orderId
    * initialRequest.data.shipments[0].shipment_id_label = shipmentCode + '-001'

    Given path '/orders'
    And header Authorization = 'Bearer '+ bearer
    And request initialRequest
    When method POST
    Then status 200

    * def response1 = $
    * def initialShipment = response1.data[0]
    * print initialShipment

    * def journeyUpdate = read('classpath:features/shipment/json/updateJourneyRQ.json')
    * journeyUpdate.data.shipment_id = initialShipment.id
    * journeyUpdate.data.journey_id = initialShipment.shipment_journey.journey_id
    * journeyUpdate.data.order_id = initialShipment.order.id
    * journeyUpdate.data.package_journey_segments[0].status = 'PLANNED'
    * journeyUpdate.data.package_journey_segments[1].vehicle.type = 'CAR'
    * journeyUpdate.data.package_journey_segments[1].vehicle.name = 'CAR001'
    * journeyUpdate.data.package_journey_segments[1].vehicle.number = 'CAR 123'
    * journeyUpdate.data.package_journey_segments[1].driver.name = 'DRIVER'
    * journeyUpdate.data.package_journey_segments[1].driver.phone_code = '+62'
    * journeyUpdate.data.package_journey_segments[1].driver.phone_number = '000-11-22'

    Given path '/shipments/shipment_journey'
    And header Authorization = 'Bearer '+ bearer
    And request journeyUpdate
    When method PUT
    Then status 200

    * def response2 = $
    * def updatedJourney = response2.data
    * def shipmentJourneyId = updatedJourney.shipment_journey.journey_id
    * print updatedJourney

    * def shipmentUpdateRequest = initialRequest
    * def multipleShipments = shipmentUpdateRequest.data.shipments
    * copy existingShipment = shipmentUpdateRequest.data.shipments[0]
    * karate.appendTo(multipleShipments, existingShipment)
    * multipleShipments[0].id = utils.uuid()
    * multipleShipments[0].shipment_id_label = shipmentCode + '-002'
    * shipmentUpdateRequest.data.shipments = multipleShipments
    * print shipmentUpdateRequest

  @Regression
  Scenario: Add new package to OM Payload
    * def bearer = token
    Given path '/orders'
    And header Authorization = 'Bearer '+ bearer
    And request shipmentUpdateRequest
    When method POST
    Then status 200

    * def response = $
    * def data = response.data
    * match data == '#present'
    * match data == '#notnull'
    * match data == '##array'
    * match data == '#[2]'

    * match response.data[0].shipment_journey == '#present'
    * match response.data[0].shipment_journey == '#notnull'
    * match response.data[0].shipment_journey.journey_id == shipmentJourneyId
    * match response.data[0].shipment_journey.package_journey_segments == '#present'
    * match response.data[0].shipment_journey.package_journey_segments == '#notnull'
    * match response.data[0].shipment_journey.package_journey_segments == '##array'
    * match response.data[0].shipment_journey.package_journey_segments == '#[3]'
