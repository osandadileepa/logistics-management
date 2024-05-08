Feature: Update Segment Status With Shipment Milestone Additional Info

  Background:
    * url baseUrl
    * def bearer = token
    * def utils = Java.type('com.quincus.karate.automation.utils.DataUtils')
    * def createShipmentResult = call read('classpath:features/shipment/addSingleWithValidDetails.feature')
    * def shipmentId = createShipmentResult.response.data.id
    * def segmentId1 = createShipmentResult.response.data.shipment_journey.package_journey_segments[0].segment_id


    * def milestoneWithLaterTime = read('classpath:features/shipment/json/updateShipmentMilestoneWithLaterTimeRQ.json')
    * milestoneWithLaterTime.data.shipment_id = shipmentId
    * milestoneWithLaterTime.data.segment_id = segmentId1
    * milestoneWithLaterTime.data.milestone_time = utils.getOffsetDateTimePlusDays(3)


    * def milestoneWithEarlierTime = read('classpath:features/shipment/json/updateShipmentMilestoneWithEarlyTimeRQ.json')
    * milestoneWithEarlierTime.data.shipment_id = shipmentId
    * milestoneWithEarlierTime.data.segment_id = segmentId1
    * milestoneWithEarlierTime.data.milestone_time = utils.getOffsetDateTimeMinusDays(3)

    * def shipment_tracking_id = createShipmentResult.data.shipment_tracking_id


  @ShipmentMilestoneOpsUpdate @Regression @Test
  Scenario: Update Segment Status With Later Time Shipment Milestone
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipment_tracking_id + '/milestone-and-additional-info')
    And header Authorization = 'Bearer '+ bearer
    And request milestoneWithLaterTime
    When method PATCH
    Then status 200


    Given url utils.decodeUrl(baseUrl + '/shipments/' + shipmentId)
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * def data = response.data
    * match data.milestone_events[0].milestone_code == '1117'
    * match data.shipment_journey.package_journey_segments[0].status == 'COMPLETED'


  @ShipmentMilestoneOpsUpdate @Regression @Test
  Scenario:Update Segment Status With Earlier Time Shipment Milestone
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipment_tracking_id + '/milestone-and-additional-info')
    And header Authorization = 'Bearer '+ bearer
    And request milestoneWithEarlierTime
    When method PATCH
    Then status 200


    Given url utils.decodeUrl(baseUrl + '/shipments/' + shipmentId)
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * def data = response.data
    * match data.milestone.milestone_code == '1100'
