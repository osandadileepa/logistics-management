Feature: Simulate Milestone Message from Dispatch

  Background:
    * url baseUrl
    * def bearer = token

    * def createShipmentResult = callonce read('classpath:features/shipment/get.feature')
    * def shipmentId = createShipmentResult.response.data.id
    * def shipmentTrackingId = createShipmentResult.response.data.shipment_tracking_id
    * def segmentId1 = createShipmentResult.response.data.shipment_journey.package_journey_segments[0].segment_id

  @SimulateMilestoneMessageWithShipmentReturnedStatusCodeFromDispatch
  @Utility
  @Regression
  Scenario: Simulate Milestone Message With Shipment Returned Status Code from Dispatch

    * def milestoneMessage = read('classpath:features/utility/json/milestoneMessageForSuccessPickup.json')
    * def currentTime = utils.getFormattedOffsetDateTimeNow()
    * milestoneMessage.shipment_id = shipmentId
    * milestoneMessage.shipment_tracking_id = shipmentTrackingId
    * milestoneMessage.segment_id = segmentId1
    * milestoneMessage.milestone_code = '1119'
    * milestoneMessage.create_time = currentTime
    * milestoneMessage.milestone_time = currentTime

    Given path '/utilities/simulate/dsp/receive-milestone'
    And header Authorization = 'Bearer '+ bearer
    And request milestoneMessage
    When method POST
    Then status 200

    * utils.sleep(5000)

    Given path '/shipments/' + shipmentId
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * def data = response.data
    * match data.milestone_events[0].milestone_code == '1119'