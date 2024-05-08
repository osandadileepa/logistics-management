Feature: Create Shipments from OM Payload Feature

  Background:
    * url baseUrl
    * def requestPath = karate.get('singleRQPath', 'classpath:features/shipment/json/omPayload.json')
    * def s2sTokenDetails = read('classpath:session/json/s2s_token_and_organization.json')
    * def updatedRequest = read(requestPath)
    * def s2sTokenDetails = read('classpath:session/json/s2s_token_and_organization.json')
    * def organizationId = s2sTokenDetails.organization_id
    * def s2sToken = s2sTokenDetails.s2s_token

    * def orderId = utils.uuid()
    * def shipmentCode = utils.uuid().substring(0, 16)

    * updatedRequest.data.id = orderId
    * updatedRequest.data.shipment_code = shipmentCode
    * updatedRequest.data.order_id_label = utils.uuid().substring(0, 10)

    * updatedRequest.data.shipments[0].order_id = orderId
    * updatedRequest.data.shipments[0].shipment_id_label = shipmentCode + '-001'

    * updatedRequest.data.shipments[1].order_id = orderId
    * updatedRequest.data.shipments[1].shipment_id_label = shipmentCode + '-002'


  @ShipmentCreateFromOMPayload
  @Regression
  Scenario: Create Shipments from OM Payload using Invalid S2S Token for Authorization
    Given path '/orders'
    And header X-API-AUTHORIZATION = 'INVALID_S2S_TOKEN'
    And header X-ORGANISATION-ID = organizationId
    And request updatedRequest
    When method POST
    Then status 401

    * def response = $

    * print response
    * def message = response.message
    * match message == '#present'
    * match message == '#notnull'
    * match message == 'Access Denied. The user does not have valid authorization to access this resource.'
