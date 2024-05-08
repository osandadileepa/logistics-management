Feature: Send Check In Message to API-G

  Background:
    * url baseUrl
    * def bearer = token
    * def createShipmentResult = callonce read('classpath:features/shipment/addSingleWithValidDetails.feature')
    * def shipmentId = createShipmentResult.response.data.id
    * def segmentId = createShipmentResult.response.data.shipment_journey.package_journey_segments[0].segment_id
    * def organizationId = createShipmentResult.response.data.milestone.organization_id

    * def milestoneMessage = read('classpath:features/utility/json/milestoneMessage.json')
    * milestoneMessage.shipment_id = shipmentId
    * milestoneMessage.segment_id = segmentId
    * milestoneMessage.organisation_id = organizationId

  @CheckIn
  @Utility
  @ignore
  Scenario: Send CheckIn Message to API-G
    Given path '/utilities/simulate/apig/check-in'
    And header Authorization = 'Bearer '+ bearer
    And request milestoneMessage
    When method POST
    Then status 200

    * def response = $
    * print response
    * def data = response.data

    * match data.message == 'Success'
    * match data.status == 'SCC0000'


