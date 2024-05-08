Feature: Simulate Milestone Message from Dispatch

  Background:
    * url baseUrl
    * def bearer = token

    * def createShipmentResult = callonce read('classpath:features/shipment/get.feature')
    * def shipmentId = createShipmentResult.response.data.id
    * def shipmentTrackingId = createShipmentResult.response.data.shipment_tracking_id
    * def segmentId1 = createShipmentResult.response.data.shipment_journey.package_journey_segments[0].segment_id

  @SimulateMilestoneMessageWithDriverArrivedForPickupStatusCodeFromDispatch
  @Utility
  @Regression
  Scenario: Simulate Milestone Message With Driver Arrived For Pickup Status Code from Dispatch

    * def milestoneMessage = read('classpath:features/utility/json/milestoneMessageForDriverArrivedForPickup.json')
    * def currentTime = utils.getFormattedOffsetDateTimeNow()
    * milestoneMessage.shipment_id = shipmentId
    * milestoneMessage.shipment_tracking_id = shipmentTrackingId
    * milestoneMessage.segment_id = segmentId1
    * milestoneMessage.create_time = currentTime
    * milestoneMessage.milestone_time = currentTime
    * def pickupOnSiteTime = milestoneMessage.milestone_time

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
    * match data.milestone_events[0].milestone_code == '1607'
    * match data.shipment_journey.package_journey_segments[0].pick_up_on_site_time == '#notnull'
    * match data.shipment_journey.package_journey_segments[0].pick_up_on_site_timezone == '#notnull'