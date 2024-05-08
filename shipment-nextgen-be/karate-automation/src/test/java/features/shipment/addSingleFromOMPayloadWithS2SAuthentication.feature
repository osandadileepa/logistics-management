Feature: Create Shipments from OM Payload Feature

  Background:
    * url baseUrl
    * def requestPath = karate.get('singleRQPath', 'classpath:features/shipment/json/omPayload.json')
    * def updatedRequest = read(requestPath)
    * def s2sTokenDetails = read('classpath:session/json/s2s_token_and_organization.json')
    * def organizationId = s2sTokenDetails.organization_id
    * def s2sToken = s2sTokenDetails.s2s_token
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
  Scenario: Create Shipments from OM Payload using S2S Token for Authorization
    Given path '/orders'
    And header X-API-AUTHORIZATION = s2sToken
    And header X-ORGANISATION-ID = organizationId
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
    * match response.data[0].shipment_journey.alerts == '#[0]'
    * match response.data[0].shipment_journey.package_journey_segments == '#present'
    * match response.data[0].shipment_journey.package_journey_segments == '#notnull'
    * match response.data[0].shipment_journey.package_journey_segments == '##array'
    * match response.data[0].shipment_journey.package_journey_segments == '#[3]'

    * match response.data[1].shipment_journey == '#notnull'
    * match response.data[1].shipment_journey == '#present'
    * match response.data[1].shipment_journey.alerts == '#[0]'
    * match response.data[1].shipment_journey.package_journey_segments == '#present'
    * match response.data[1].shipment_journey.package_journey_segments == '#notnull'
    * match response.data[1].shipment_journey.package_journey_segments == '##array'
    * match response.data[0].shipment_journey.package_journey_segments == '#[3]'

    * match response.data[0].shipment_package.commodities[0].name == '#notnull'
    * match response.data[0].shipment_package.commodities[0].value == '#notnull'
    * match response.data[0].shipment_package.commodities[0].description == '#notnull'
    * match response.data[0].shipment_package.commodities[0].code == '#notnull'
    * match response.data[0].shipment_package.commodities[0].hs_code == '#notnull'
    * match response.data[0].shipment_package.commodities[0].note == '#notnull'
    * match response.data[0].shipment_package.commodities[0].packaging_type == '#notnull'

    * match response.data[0].origin.company == '#notnull'
    * match response.data[0].origin.department == '#notnull'
    * match response.data[0].destination.company == '#notnull'
    * match response.data[0].destination.department == '#notnull'

    * def firstSegment = response.data[0].shipment_journey.package_journey_segments[0]
    * match firstSegment.instructions == '#[1]'
    * match firstSegment.instructions[0].external_id == '#notnull'
    * match firstSegment.instructions[0].label == '#notnull'
    * match firstSegment.instructions[0].source == '#notnull'
    * match firstSegment.instructions[0].value == '#notnull'
    * match firstSegment.instructions[0].apply_to == '#notnull'
    * match firstSegment.instructions[0].created_at == '#notnull'
    * match firstSegment.instructions[0].updated_at == '#notnull'

    * def lastSegment = response.data[0].shipment_journey.package_journey_segments[2]
    * match lastSegment.instructions == '#[1]'
    * match lastSegment.instructions[0].external_id == '#notnull'
    * match lastSegment.instructions[0].label == '#notnull'
    * match lastSegment.instructions[0].source == '#notnull'
    * match lastSegment.instructions[0].value == '#notnull'
    * match lastSegment.instructions[0].apply_to == '#notnull'
    * match lastSegment.instructions[0].created_at == '#notnull'
    * match lastSegment.instructions[0].updated_at == '#notnull'
