Feature: Create Shipments from OM Payload (missing mandatory fields) Feature

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

    # Mandatory field set to null
    * updatedRequest.data.ops_type = null
    * updatedRequest.data.segments_payload[1].departure_time = null
    * updatedRequest.data.segments_payload[2].partner_id = null


  @ShipmentCreateFromOMPayload
  @Regression
  Scenario: Create Shipments from OM Payload with missing mandatory fields
    * def bearer = token
    Given path '/orders'
    And header Authorization = 'Bearer '+ bearer
    And request updatedRequest
    When method POST
    Then status 200

    * def response = $

    * print response

    * match response.data == '#present'
    * match response.data == '#notnull'
    * match response.data == '##array'
    * match response.data == '#[2]'

    * match response.data[0].shipment_journey == '#present'
    * match response.data[0].shipment_journey == '#notnull'
    * match response.data[0].shipment_journey.alerts == '#[1]'
    * match response.data[0].shipment_journey.alerts[0].short_message == 'Blank mandatory field'
    * match response.data[0].shipment_journey.alerts[0].message == 'Mandatory field(s) is blank. [Segment 1][Segment 2][Segment 3]'
    * match response.data[0].shipment_journey.alerts == '#[1]'
    * match response.data[0].shipment_journey.package_journey_segments == '#present'
    * match response.data[0].shipment_journey.package_journey_segments == '#notnull'
    * match response.data[0].shipment_journey.package_journey_segments == '##array'
    * match response.data[0].shipment_journey.package_journey_segments == '#[3]'
    * match response.data[0].shipment_journey.package_journey_segments[0].alerts == '#[1]'
    * match response.data[0].shipment_journey.package_journey_segments[0].alerts[0].short_message == 'Blank mandatory field'
    * match response.data[0].shipment_journey.package_journey_segments[0].alerts[0].message == 'Mandatory field(s) is blank.'
    * match response.data[0].shipment_journey.package_journey_segments[0].alerts[0].fields[0] == 'ops_type'

    * match response.data[0].shipment_journey.package_journey_segments[1].alerts == '#[1]'
    * match response.data[0].shipment_journey.package_journey_segments[1].alerts[0].short_message == 'Blank mandatory field'
    * match response.data[0].shipment_journey.package_journey_segments[1].alerts[0].message == 'Mandatory field(s) is blank.'
    * match response.data[0].shipment_journey.package_journey_segments[1].alerts[0].fields[0] == 'ops_type'
    * match response.data[0].shipment_journey.package_journey_segments[1].alerts[0].fields[1] == 'departure_time'

    * match response.data[0].shipment_journey.package_journey_segments[2].alerts == '#[1]'
    * match response.data[0].shipment_journey.package_journey_segments[2].alerts[0].short_message == 'Blank mandatory field'
    * match response.data[0].shipment_journey.package_journey_segments[2].alerts[0].message == 'Mandatory field(s) is blank.'
    * match response.data[0].shipment_journey.package_journey_segments[2].alerts[0].fields[0] == 'ops_type'
    * match response.data[0].shipment_journey.package_journey_segments[2].alerts[0].fields[1] == 'partner'

    * match response.data[1].shipment_journey == '#notnull'
    * match response.data[1].shipment_journey == '#present'
    * match response.data[1].shipment_journey.alerts == '#[1]'
    * match response.data[1].shipment_journey.package_journey_segments == '#present'
    * match response.data[1].shipment_journey.package_journey_segments == '#notnull'
    * match response.data[1].shipment_journey.package_journey_segments == '##array'
    * match response.data[0].shipment_journey.package_journey_segments == '#[3]'
