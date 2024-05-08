Feature: Simulate Milestone Message from Dispatch

  Background:
    * url baseUrl
    * def bearer = token

    * def createShipmentResult = callonce read('classpath:features/shipment/get.feature')
    * def shipmentId = createShipmentResult.response.data.id
    * def shipmentTrackingId = createShipmentResult.response.data.shipment_tracking_id
    * def segmentId = createShipmentResult.response.data.shipment_journey.package_journey_segments[0].segment_id

    * def milestoneMessage = read('classpath:features/utility/json/milestoneMessage.json')
    * milestoneMessage.shipment_id = shipmentId
    * milestoneMessage.shipment_tracking_id = shipmentTrackingId
    * milestoneMessage.milestone_code = 1510
    * milestoneMessage.milestone_time = utils.getOffsetDateTimePlusDays(1)
    * milestoneMessage.segment_id = segmentId


  @SimulateMilestoneMessageFromDispatch
  @Utility
  @ignore
  Scenario: Simulate Milestone Message from Dispatch
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
    * print response
    * def data = response.data
    * match data.milestone_events == '#[2]'
    * match data.milestone_events[1].milestone_code == '1100'
    * match data.shipment_journey.package_journey_segments[0].status == 'IN_PROGRESS'

    * match data.milestone_events[0].additional_info.images == '#[2]'
    * match data.milestone_events[0].additional_info.images[0].file_name == '#present'
    * match data.milestone_events[0].additional_info.images[0].file_url == '#present'
    * match data.milestone_events[0].additional_info.images[0].file_size == '#present'
    * match data.milestone_events[0].additional_info.images[0].file_timestamp == '#present'
    * match data.milestone_events[0].additional_info.images[1].file_name == '#present'
    * match data.milestone_events[0].additional_info.images[1].file_url == '#present'
    * match data.milestone_events[0].additional_info.images[1].file_size == '#present'
    * match data.milestone_events[0].additional_info.images[1].file_timestamp == '#present'
    * match data.milestone_events[0].additional_info.signature == '#[1]'
    * match data.milestone_events[0].additional_info.signature[0].file_name == '#present'
    * match data.milestone_events[0].additional_info.signature[0].file_url == '#present'
    * match data.milestone_events[0].additional_info.signature[0].file_size == '#present'
    * match data.milestone_events[0].additional_info.signature[0].file_timestamp == '#present'
