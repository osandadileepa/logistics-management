Feature: Weight Calculation

  Background:
    * url baseUrl
    * path '/weight-calculation-rules'
    * def requestJson = read('classpath:features/weight-calculation-rule/json/request.json')
    * multipart field data = requestJson
    * method POST
    * def standardRule = response.data

    * path '/weight-calculation-rules'
    * requestJson.data.volume_weight_rule = 'SPECIAL'
    * multipart field data = requestJson
    * multipart file file = { read: 'classpath:features/weight-calculation-rule/template/csvTemplate.csv', contentType: 'text/csv' }
    * method POST
    * def specialRule = response.data

    * def calculateRequest = read('json/request.json')
    * path '/weight-calculation'

  @WeightCalculation
  Scenario: weight calculation with standard volume weight calculation
    Given calculateRequest.data.rule_id = standardRule.id
    And request calculateRequest
    When method POST
    Then status 200

    * match response.data.chargeable_weight == 24.0000
    * match response.data.actual_weight == 20
    * match response.data.volume_weight == 24.0000
    * match response.data.rule_applied == standardRule.name
    * match response.data.rule_id == standardRule.id

  @WeightCalculation
  Scenario: weight calculation with special volume weight calculation
    Given calculateRequest.data.rule_id = specialRule.id
    And request calculateRequest
    When method POST
    Then status 200

    * print response

    * match response.data.chargeable_weight == 8.0
    * match response.data.actual_weight == 20
    * match response.data.volume_weight == 8.0
    * match response.data.rule_applied == specialRule.name
    * match response.data.rule_id == specialRule.id

  @WeightCalculation
  Scenario: weight calculation with special volume weight calculation and no match in conversion table
    Given request calculateRequest
    And calculateRequest.data.rule_id = specialRule.id
    And calculateRequest.data.length = 13
    When method POST
    Then status 200

    * match response.data.chargeable_weight == 24.0000
    * match response.data.actual_weight == 20
    * match response.data.volume_weight == 24.0000
    * match response.data.rule_applied == specialRule.name
    * match response.data.rule_id == specialRule.id