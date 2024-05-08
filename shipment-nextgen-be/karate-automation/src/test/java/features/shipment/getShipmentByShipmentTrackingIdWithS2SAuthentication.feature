Feature: Create Shipments from OM Payload Feature

  Background:
    * url baseUrl
    * def jsonPath = 'classpath:features/shipment/json/omPayload.json'
    * def createShipmentResult = call read('classpath:features/shipment/addSingleFromOMPayloadWithS2SAuthentication.feature') {singleRQPath : '#(jsonPath)'}
    * def shipment = createShipmentResult.data[0]
    * def id = shipment.id
    * def shipmentTrackingId = shipment.shipment_tracking_id
    * print id
    * print shipmentTrackingId

    * def s2sTokenDetails = read('classpath:session/json/s2s_token_and_organization.json')
    * def organizationId = s2sTokenDetails.organization_id
    * def s2sToken = s2sTokenDetails.s2s_token

  @ShipmentFindByTrackingId @Regression
  Scenario: Retrieve a Shipment By Shipment Tracking Id
    Given path '/shipments'
    And param shipment_tracking_id = shipmentTrackingId
    And header X-API-AUTHORIZATION = s2sToken
    And header X-ORGANISATION-ID = organizationId
    When method GET
    Then status 200

    * def response = $
    * print response
    * match response.data.id == id
    * match response.data.shipment_tracking_id == shipmentTrackingId
    * match response.data.created_time == '#present'
    * match response.data.created_time == '#notnull'
    * match response.data.last_updated_time == '#present'
    * match response.data.last_updated_time == '#notnull'