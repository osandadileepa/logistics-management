Feature: Simulate Milestone Message from Dispatch

  Background:
    * url baseUrl
    * def bearer = token

    * def createShipmentResult = callonce read('classpath:features/shipment/get.feature')
    * def shipmentId = createShipmentResult.response.data.id
    * def shipmentTrackingId = createShipmentResult.response.data.shipment_tracking_id
    * def segmentId = createShipmentResult.response.data.shipment_journey.package_journey_segments[0].segment_id
    * def organizationId = createShipmentResult.response.data.organization.id

    * def milestoneMessage = read('classpath:features/utility/json/milestoneMessage.json')
    * milestoneMessage.shipment_id = shipmentId
    * milestoneMessage.shipment_tracking_id = shipmentTrackingId
    * milestoneMessage.segment_id = segmentId
    * milestoneMessage.organisation_id = organizationId

  @SimulateMilestoneMessageFromDispatch
  @Utility
  @ignore
  Scenario: Simulate 3 consecutive Milestone Message with failed status code from Dispatch

    # send first failed status code
    Given path '/utilities/simulate/dsp/receive-milestone'
    And header Authorization = 'Bearer '+ bearer
    And request milestoneMessage
    When method POST
    Then status 200

    # send second failed status code
    Given path '/utilities/simulate/dsp/receive-milestone'
    And header Authorization = 'Bearer '+ bearer
    And request milestoneMessage
    When method POST
    Then status 200

    * utils.sleep(5000)

    # verify milestone events are created, but no alert yet
    Given path '/shipments/' + shipmentId
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * def data = response.data
    * match data.milestone_events == '#[3]'
    * match data.shipment_journey.alerts == '#[0]'

    # send third failed status code
    Given path '/utilities/simulate/dsp/receive-milestone'
    And header Authorization = 'Bearer '+ bearer
    And request milestoneMessage
    When method POST
    Then status 200

    * utils.sleep(5000)

    # verify milestone event and alert is created
    Given path '/shipments/' + shipmentId
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * def data = response.data
    * match data.milestone_events == '#[4]'
    * match data.shipment_journey.alerts == '#[1]'
    * match data.shipment_journey.alerts[0].message == 'Pick up/delivery failed. Please update the details in the journey segment.'
    * match data.shipment_journey.alerts[0].type == 'JOURNEY_REVIEW_REQUIRED'
    * match data.shipment_journey.alerts[0].dismissed == false


