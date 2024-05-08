Feature: Weight calculation with organization and partner scenario

  Background:
    * url baseUrl
    * path '/weight-calculation-rules'
    * def requestJson = read('classpath:features/weight-calculation-rule/json/request.json')
    * def uuid = function(){ return java.util.UUID.randomUUID().toString() }

    * def orgId_1 = uuid()
    * def partnerId_1 = uuid()
    * def partnerId_2 = uuid()

    * requestJson.data.organization_id = orgId_1
    * requestJson.data.partners[0].id = partnerId_1
    * requestJson.data.partners[1].id = partnerId_2
    * requestJson.data.active = true
    * multipart field data = requestJson
    * method POST
    * def rule = response.data

    * def calculateRequest = read('json/request.json')
    * path '/weight-calculation'

  @WeightCalculation
  Scenario: weight calculation with standard volume weight calculation with organization and partner
    Given calculateRequest.data.rule_id = null
    And calculateRequest.data.organization_id = orgId_1
    And calculateRequest.data.partner_id = partnerId_1
    And request calculateRequest
    When method POST
    Then status 200

    * match response.data.chargeable_weight == 24.0000
    * match response.data.actual_weight == 20
    * match response.data.volume_weight == 24.0000
    * match response.data.rule_applied == rule.name
    * match response.data.rule_id == rule.id
