Feature: Delete Weight Calculation Rule

  Background:
    * url baseUrl
    * path '/weight-calculation-rules'
    * def requestJson = read('json/request.json')
    * multipart field data = requestJson
    * method POST
    * def id = response.data.id

  @WeightCalculationRuleDelete
  Scenario: delete an existing weight calculation rule
    Given path '/weight-calculation-rules/' + id
    When method DELETE
    Then status 200