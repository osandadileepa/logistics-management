Feature: Create Shipments from OM Payload Feature

  Background:
    * url baseUrl
    * def uploadNetworkLaneResult = call read('classpath:features/attachments/networklane/uploadBulk.feature')
    * def requestPath = karate.get('singleRQPath', 'classpath:features/shipment/json/omPayloadWithNetworkLaneAndNoSegments.json')
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

  @ShipmentCreateFromOMPayloadWithoutSegmentHavingNetwork
  @Regression
  @Ignore
# TODO: UnIgnore once inserting of networklane is implemented and remove the background call of upload and change it to insert
  Scenario: Create Shipments from OM Payload Without Segments Having NetworkLane
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
    * match response.data[0].shipment_journey.status == 'PLANNED'
    * match response.data[0].shipment_journey.package_journey_segments == '#present'
    * match response.data[0].shipment_journey.package_journey_segments == '#[3]'
    * match response.data[0].shipment_journey.package_journey_segments[0].status == 'PLANNED'
    * match response.data[0].shipment_journey.package_journey_segments[0].pick_up_time == '2023-05-23T00:00:00-07:00'
    * match response.data[0].shipment_journey.package_journey_segments[0].drop_off_time == '2023-05-23T00:30:00-07:00'
    * match response.data[0].shipment_journey.package_journey_segments[1].status == 'PLANNED'
    * match response.data[0].shipment_journey.package_journey_segments[1].pick_up_time == '2023-05-23T00:30:00-07:00'
    * match response.data[0].shipment_journey.package_journey_segments[1].drop_off_time == '2023-05-23T00:40:00-07:00'
    * match response.data[0].shipment_journey.package_journey_segments[2].status == 'PLANNED'
    * match response.data[0].shipment_journey.package_journey_segments[2].pick_up_time == '2023-05-23T00:40:00-07:00'
    * match response.data[0].shipment_journey.package_journey_segments[2].drop_off_time == '2023-05-23T01:00:00-07:00'


    * match response.data[1].shipment_journey == '#present'
    * match response.data[1].shipment_journey == '#notnull'
    * match response.data[1].shipment_journey.journey_id == '#present'
    * match response.data[1].shipment_journey.journey_id == '#notnull'
    * match response.data[1].shipment_journey.status == 'PLANNED'
    * match response.data[1].shipment_journey.package_journey_segments == '#present'
    * match response.data[1].shipment_journey.package_journey_segments == '#[3]'
    * match response.data[1].shipment_journey.package_journey_segments[0].status == 'PLANNED'
    * match response.data[0].shipment_journey.package_journey_segments[0].pick_up_time == '2023-05-23T00:00:00-07:00'
    * match response.data[0].shipment_journey.package_journey_segments[0].drop_off_time == '2023-05-23T00:30:00-07:00'
    * match response.data[1].shipment_journey.package_journey_segments[1].status == 'PLANNED'
    * match response.data[0].shipment_journey.package_journey_segments[1].pick_up_time == '2023-05-23T00:30:00-07:00'
    * match response.data[0].shipment_journey.package_journey_segments[1].drop_off_time == '2023-05-23T00:40:00-07:00'
    * match response.data[1].shipment_journey.package_journey_segments[2].status == 'PLANNED'
    * match response.data[0].shipment_journey.package_journey_segments[2].pick_up_time == '2023-05-23T00:40:00-07:00'
    * match response.data[0].shipment_journey.package_journey_segments[2].drop_off_time == '2023-05-23T01:00:00-07:00'
