Feature: Weight calculation with organization only scenario

  Background:
    * url baseUrl
    * path '/weight-calculation-rules'
    * def requestJson = read('classpath:features/weight-calculation-rule/json/request.json')
    * def uuid = function(){ return java.util.UUID.randomUUID().toString() }

    * def orgId_1 = uuid()

    * requestJson.data.organization_id = orgId_1
    * requestJson.data.active = true
    * requestJson.data.partners = []
    * multipart field data = requestJson
    * method POST
    * def rule = response.data

    * def calculateRequest = read('json/request.json')
    * path '/weight-calculation'

  @WeightCalculation
  Scenario: weight calculation with standard volume weight calculation with organization only
    Given calculateRequest.data.rule_id = null
    And calculateRequest.data.organization_id = orgId_1
    And calculateRequest.data.partner_id = null
    And request calculateRequest
    When method POST
    Then status 200

    * match response.data.chargeable_weight == 24.0000
    * match response.data.actual_weight == 20
    * match response.data.volume_weight == 24.0000
    * match response.data.rule_applied == rule.name
    * match response.data.rule_id == rule.id
