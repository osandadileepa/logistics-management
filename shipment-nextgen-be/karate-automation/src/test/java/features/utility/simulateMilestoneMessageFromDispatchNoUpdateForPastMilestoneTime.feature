Feature: Simulate Milestone Message To Update Segment Status from Dispatch

  Background:
    * url baseUrl
    * def bearer = token
    * def utils = Java.type('com.quincus.karate.automation.utils.DataUtils')
    * def shipmentDetails = callonce read('classpath:features/shipment/get.feature')
    * def shipment = shipmentDetails.response.data
    * def shipmentId = shipment.id
    * def shipmentTrackingId = shipment.shipment_tracking_id
    * def segmentId1 = shipment.shipment_journey.package_journey_segments[0].segment_id
    * def segmentId2 = shipment.shipment_journey.package_journey_segments[1].segment_id
    * def segmentId3 = shipment.shipment_journey.package_journey_segments[2].segment_id

    * def milestoneMessageWithEarlyTime = read('classpath:features/utility/json/milestoneMessageWithEarlyMilestoneTimeOverRecentMilestone.json')
    * milestoneMessageWithEarlyTime.shipment_id = shipmentId
    * milestoneMessageWithEarlyTime.shipment_tracking_id = shipmentTrackingId
    * milestoneMessageWithEarlyTime.segment_id = segmentId1
    * milestoneMessageWithEarlyTime.milestone_time = utils.getOffsetDateTimeMinusDays(3)

    * def findAllRequest = read('classpath:features/shipment/json/listRQ.json')
    * findAllRequest.data.keys = [shipmentTrackingId]


  @SimulateMilestoneMessageFromDispatchForSegmentStatus
  @Regression
  Scenario: Simulate Milestone Message from Dispatch For Segment Status With Early Milestone Time
    Given path '/utilities/simulate/dsp/receive-milestone'
    And header Authorization = 'Bearer '+ bearer
    And request milestoneMessageWithEarlyTime
    When method POST
    Then status 200

    * utils.sleep(5000)

    Given url utils.decodeUrl(baseUrl + '/shipments/' + shipmentId)
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * def data = response.data
    * match data.shipment_journey.package_journey_segments[0].status == 'IN_PROGRESS'
    * match data.milestone.milestone_code == '1100'
    * match data.milestone.milestone_name == 'Order Booked'


    Given url utils.decodeUrl(baseUrl + '/shipments/list')
    And header Authorization = 'Bearer '+ bearer
    And request findAllRequest
    When method POST
    Then status 200

    * def response = $
    * def data = response.data
    * match data.result[0].milestone.milestone_name == 'Order Booked'