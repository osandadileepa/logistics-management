Feature: Create Shipments from OM Payload Feature

  Background:
    * def skipSegmentOrderResponse = callonce read('classpath:features/shipment/addSingleFromOMPayloadWithUserOrgThatSkipsSegment.feature')
    * def existingShipmentId =  skipSegmentOrderResponse.response.data[0].id
    * def existingOrderId =  skipSegmentOrderResponse.response.data[0].order.id
    * def existingOrderIdLabel =  skipSegmentOrderResponse.response.data[0].order.order_id_label
    * def existingShipmentTrackingId =  skipSegmentOrderResponse.response.data[0].shipment_tracking_id

    * url baseUrl
    * def requestPath = karate.get('singleRQPath', 'classpath:features/shipment/json/shpv22Org_ompayload.json')
    * def updateRequest = read(requestPath)

    * updateRequest.data.segments_updated = true
    * updateRequest.data.id = existingOrderId
    * updateRequest.data.order_id_label = existingOrderIdLabel

    * updateRequest.data.shipments[0].order_id = existingOrderId
    * updateRequest.data.shipments[0].shipment_id_label = existingShipmentTrackingId

    * def userCredentials = 'classpath:session/json/ups_org_user.json'
    * def newSession = callonce read('classpath:session/create.feature') {userCredentials : '#(userCredentials)'}
    * def newToken = newSession.response.data.token

  @ShipmentCreateFromOMPayload
  @Regression
  Scenario: Update Segment from OM Payload with User of Organization to skip Segment when create
    Given path '/orders'
    And header Authorization = 'Bearer '+ newToken
    And request updateRequest
    When method POST
    Then status 200

    Given url utils.decodeUrl(baseUrl + '/shipments/' + existingShipmentId)
    And header Authorization = 'Bearer '+ newToken
    When method GET
    Then status 200

    * def response = $
    * print response
    * match response.data.shipment_journey == '#present'
    * match response.data.shipment_journey == '#notnull'
    * match response.data.shipment_journey.package_journey_segments == '#present'
    * match response.data.shipment_journey.package_journey_segments == '#notnull'
    * match response.data.shipment_journey.package_journey_segments == '##array'
    * match response.data.shipment_journey.package_journey_segments == '#[3]'