Feature: Generate MilestoneMessage payload for shipment-milestone topic Triggered from DSP

  Background:
    * url baseUrl
    * def bearer = token
    * def createShipmentResult = callonce read('classpath:features/shipment/get.feature')
    * def shipmentId = createShipmentResult.response.data.id
    * def shipmentTrackingId = createShipmentResult.response.data.shipment_tracking_id
    * def organization = createShipmentResult.response.data.organization
    * def userId = createShipmentResult.response.data.user_id
    * def milestone = createShipmentResult.response.data.milestone
    * def shipmentJourney = createShipmentResult.response.data.shipment_journey
    * def order = createShipmentResult.response.data.order
    * def shipmentPackage = createShipmentResult.response.data.shipment_package

    * def milestonemessage = read('classpath:features/utility/json/milestonefromdsp.json')
    * milestonemessage.data.shipment_id = shipmentId
    * milestonemessage.data.shipment_tracking_id = shipmentTrackingId
    * milestonemessage.data.segment_id = shipmentJourney.package_journey_segments[0].segment_id

  @Utility
  @SendMilestoneUpdateFromDSP
  Scenario: Get First Segment then next segment
    Given path '/utilities/simulate/milestone-update-from-dsp'
    And header Authorization = 'Bearer '+ bearer
    And request milestonemessage
    When method POST
    Then status 200

    * def response = $
    * print response
    * def data = response.data

    * match data.shipment_id == shipmentId
    * match data.segment_id == shipmentJourney.package_journey_segments[0].segment_id
    * match data.milestone_code == '1505'
    * match data.triggered_from == 'DSP'
    * match data.from_country_id == shipmentJourney.package_journey_segments[0].start_facility.location.country_id
    * match data.from_state_id == shipmentJourney.package_journey_segments[0].start_facility.location.state_id
    * match data.from_city_id == shipmentJourney.package_journey_segments[0].start_facility.location.city_id
    * match data.to_country_id == shipmentJourney.package_journey_segments[0].end_facility.location.country_id
    * match data.to_state_id == shipmentJourney.package_journey_segments[0].end_facility.location.state_id
    * match data.to_city_id == shipmentJourney.package_journey_segments[0].end_facility.location.city_id