Feature: Weight Calculation

  Background:
    * url baseUrl
    * path '/weight-calculation-rules'
    * def requestJson = read('classpath:features/weight-calculation-rule/json/request.json')

    * requestJson.data.chargeable_weight_rule = 'HIGHER_VALUE_BETWEEN_ACTUAL_AND_VOLUME_WEIGHT'
    * requestJson.data.chargeable_weight_rounding = true
    * requestJson.data.actual_weight_rounding = true
    * requestJson.data.volume_weight_rounding = true

    * multipart field data = requestJson
    * method POST
    * def rule = response.data

    * def calculateRequest = read('json/request.json')
    * path '/weight-calculation'

  @WeightCalculation
  Scenario: calculate with rounding logic
    Given calculateRequest.data.rule_id = rule.id
    And calculateRequest.data.length = 12.345
    And calculateRequest.data.width = 67.8
    And calculateRequest.data.height = 9
    And calculateRequest.data.actual_weight = 3766.9999
    And request calculateRequest
    When method POST
    Then status 200

    * match response.data.chargeable_weight == 3767
    * match response.data.actual_weight == 3767
    * match response.data.volume_weight == 3766
    * match response.data.rule_applied == rule.name
    * match response.data.rule_id == rule.id