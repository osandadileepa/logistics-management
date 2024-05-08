Feature: Create Shipments from OM Payload Feature

  Background:
    * def userCredentials = 'classpath:session/json/ups_org_user.json'
    * def newSession = callonce read('classpath:session/create.feature') {userCredentials : '#(userCredentials)'}
    * def newToken = newSession.response.data.token

    * url baseUrl
    * def requestPath = karate.get('singleRQPath', 'classpath:features/shipment/json/shpv22Org_ompayload.json')
    * def updatedRequest = read(requestPath)
    * def orderId = utils.uuid()
    * def shipmentCode = utils.uuid().substring(0, 16)

    * updatedRequest.data.id = orderId
    * updatedRequest.data.shipment_code = shipmentCode
    * updatedRequest.data.order_id_label = utils.uuid().substring(0, 10)

    * updatedRequest.data.shipments[0].order_id = orderId
    * updatedRequest.data.shipments[0].shipment_id_label = shipmentCode + '-001'

  @ShipmentCreateFromOMPayload
  @Regression
  Scenario: Create Shipments from OM Payload with User of Organization to skip Segment creation from payload
    Given path '/orders'
    And header Authorization = 'Bearer '+ newToken
    And request updatedRequest
    When method POST
    Then status 200

    * def response = $

    * print response
    * def data = response.data

    * match data == '#present'
    * match data == '#notnull'
    * match data == '##array'
    * match data == '#[1]'

    * match response.data[0].shipment_journey == '#present'
    * match response.data[0].shipment_journey == '#notnull'
    * match response.data[0].shipment_journey.package_journey_segments == '#present'
    * match response.data[0].shipment_journey.package_journey_segments == '#notnull'
    * match response.data[0].shipment_journey.package_journey_segments == '##array'
    * match response.data[0].shipment_journey.package_journey_segments == '#[1]'
    * match response.data[0].shipment_journey.package_journey_segments[0].start_facility.name == 'origin'
    * match response.data[0].shipment_journey.package_journey_segments[0].end_facility.name == 'destination'
