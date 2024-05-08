Feature: Generate MilestoneMessage payload for shipment-milestone topic Triggered from OM

  Background:
    * url baseUrl
    * def bearer = token
    * def createShipmentResult = callonce read('classpath:features/shipment/addSingle.feature')
    * print createShipmentResult
    * def shipmentId = createShipmentResult.response.data.id
    * def organization = createShipmentResult.response.data.organization
    * def userId = createShipmentResult.response.data.user_id
    * def milestone = createShipmentResult.response.data.milestone
    * def shipmentJourney = createShipmentResult.response.data.shipment_journey
    * def order = createShipmentResult.response.data.order
    * def shipmentPackage = createShipmentResult.response.data.shipment_package

    * def defaultshipment = read('classpath:features/utility/json/defaultshipment.json')
    * defaultshipment.data.id = shipmentId
    * defaultshipment.data.order = order
    * defaultshipment.data.user_id = userId
    * defaultshipment.data.organization.id = milestone.organization_id
    * defaultshipment.data.shipment_package = shipmentPackage
    * defaultshipment.data.milestone = milestone
    * defaultshipment.data.shipment_journey = shipmentJourney

  @Utility
  @SendMilestoneUpdateFromOMorSHP
  Scenario: Get First Segment then next segment
    Given path '/utilities/simulate/milestone-update-from-shp-or-om'
    And header Authorization = 'Bearer '+ bearer
    And request defaultshipment
    When method POST
    Then status 200

    * def response = $
    * print response
    * def data = response.data

    * match data.shipment_id == shipmentId
    * match data.segment_id == shipmentJourney.package_journey_segments[0].segment_id
    * match data.milestone_code == '1100'
    * match data.triggered_from == 'OM'
    * match data.from_country_id == shipmentJourney.package_journey_segments[0].start_facility.location.country_id
    * match data.from_state_id == shipmentJourney.package_journey_segments[0].start_facility.location.state_id
    * match data.from_city_id == shipmentJourney.package_journey_segments[0].start_facility.location.city_id
    * match data.to_country_id == shipmentJourney.package_journey_segments[0].end_facility.location.country_id
    * match data.to_state_id == shipmentJourney.package_journey_segments[0].end_facility.location.state_id
    * match data.to_city_id == shipmentJourney.package_journey_segments[0].end_facility.location.city_id

    * defaultshipment.data.shipment_journey.package_journey_segments[0].status = 'COMPLETED'
    Given path '/utilities/simulate/milestone-update-from-shp-or-om'
    And header Authorization = 'Bearer '+ bearer
    And request defaultshipment
    When method POST
    Then status 200

    * def response = $
    * print response
    * def data = response.data
    * match data.shipment_id == shipmentId
    * match data.segment_id == shipmentJourney.package_journey_segments[1].segment_id
    * match data.milestone_code == '1100'
    * match data.triggered_from == 'OM'
    * match data.from_country_id == shipmentJourney.package_journey_segments[1].start_facility.location.country_id
    * match data.from_state_id == shipmentJourney.package_journey_segments[1].start_facility.location.state_id
    * match data.from_city_id == shipmentJourney.package_journey_segments[1].start_facility.location.city_id
    * match data.to_country_id == shipmentJourney.package_journey_segments[1].end_facility.location.country_id
    * match data.to_state_id == shipmentJourney.package_journey_segments[1].end_facility.location.state_id
    * match data.to_city_id == shipmentJourney.package_journey_segments[1].end_facility.location.city_id


