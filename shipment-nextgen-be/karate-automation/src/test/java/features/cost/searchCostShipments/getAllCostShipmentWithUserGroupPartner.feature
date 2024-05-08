Feature: Cost Shipment Search With No User Group: Partner Scenario

  Background:
    * url baseUrl
    * def bearer = token

    * def userPartner = 'classpath:session/json/partner_user_001.json'
    * def newSessionA = callonce read('classpath:session/create.feature') {userCredentials : '#(userPartner)'}
    * def newTokenA = newSessionA.response.data.token

    * def noUserPartner = 'classpath:session/json/no_location_coverage.json'
    * def newSession = callonce read('classpath:session/create.feature') {userCredentials : '#(noUserPartner)'}
    * def newTokenB = newSession.response.data.token

    * def userGroupPartnerId = 'be41de94-f039-4cbd-ae0c-08ad45fd1ca7'

    * def searchRequest = read('classpath:features/shipment/json/search/keysTrackingIdsRQ.json')
    * searchRequest.data.keys = []

  @CostShipmentSearch @Regression
  Scenario: Cost Shipment Search with User Group Partner should retrieve all shipments with no associated partners and particular partner
    * def bearer = newTokenA
    Given url baseUrl + '/costs/shipments/search'
    And header Authorization = 'Bearer '+ bearer
    And request searchRequest
    When method POST
    Then status 200

    * def response = $
    * print response

    * def data = response.data
    * match data.result contains deep { partner_id : 'be41de94-f039-4cbd-ae0c-08ad45fd1ca7' }

  @CostShipmentSearch @Regression
  Scenario: Cost Shipment Search without User Group Partner should retrieve all shipments with no associated partners
    * def bearer = newTokenB
    Given url baseUrl + '/costs/shipments/search'
    And header Authorization = 'Bearer '+ bearer
    And request searchRequest
    When method POST
    Then status 200

    * def response = $
    * print response
    * def data = response.data
    * match each data.result !contains deep { partner_id : userGroupPartnerId }
