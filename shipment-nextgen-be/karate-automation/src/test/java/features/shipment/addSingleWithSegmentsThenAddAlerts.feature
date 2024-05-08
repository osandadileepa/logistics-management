Feature: Create a Single Shipment with Segment Then Add Alerts Feature

  Background:
    * url baseUrl
    * def requestPath = karate.get('singleRQPath', 'classpath:features/shipment/json/single/shipmentWithSegmentsThenAddAlerts.json')
    * def updatedCreateRequest = read(requestPath)
    * updatedCreateRequest.data.shipment_tracking_id = 'SHP' + utils.uuid().substring(0, 12)
    * updatedCreateRequest.data.order.id = utils.uuid()
    * updatedCreateRequest.data.order.order_id_label = utils.uuid().substring(0, 10)

    * def segments = updatedCreateRequest.data.shipment_journey.package_journey_segments

  @ShipmentCreateWithSegmentsAndAlerts @SegmentRegression
  Scenario: Create Shipment with Segments
    * def bearer = token
    * def shipmentTrackingID = utils.uuid()
    Given path '/shipments'
    And header Authorization = 'Bearer '+ bearer
    And request updatedCreateRequest
    When method POST
    Then status 200

    * def response = $
    * print response

    * def data = response.data

    * match data.shipment_journey.package_journey_segments == '#[3]'
    * match data.shipment_journey.package_journey_segments[0].alerts[0].message == 'Mandatory fields are blank.'
    * match data.shipment_journey.package_journey_segments[0].alerts[0].type == 'ERROR'
    * match data.shipment_journey.package_journey_segments[0].alerts[0].dismissed == false
    * match data.shipment_journey.package_journey_segments[0].alerts[0].fields[0] == 'ops_type'
    * match data.shipment_journey.package_journey_segments[1].alerts[0].message == 'Mandatory fields are blank.'
    * match data.shipment_journey.package_journey_segments[1].alerts[0].type == 'ERROR'
    * match data.shipment_journey.package_journey_segments[1].alerts[0].dismissed == false
    * match data.shipment_journey.package_journey_segments[1].alerts[0].fields[0] == 'ops_type'
    * match data.shipment_journey.package_journey_segments[1].alerts[0].fields[1] == 'pick_up_commit_time'
    * match data.shipment_journey.package_journey_segments[1].alerts[0].fields[2] == 'drop_off_commit_time'
    * match data.shipment_journey.package_journey_segments[2].alerts[0].message == 'Mandatory fields are blank.'
    * match data.shipment_journey.package_journey_segments[2].alerts[0].type == 'ERROR'
    * match data.shipment_journey.package_journey_segments[2].alerts[0].dismissed == false
    * match data.shipment_journey.package_journey_segments[2].alerts[0].fields[0] == 'ops_type'

    * match data.shipment_journey.alerts[0].message == 'Mandatory fields are blank. [Segment 1][Segment 2][Segment 3]'
    * match data.shipment_journey.alerts[0].type == 'ERROR'
