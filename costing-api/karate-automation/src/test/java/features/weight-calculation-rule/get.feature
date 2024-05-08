Feature: Get Weight Calculation Rule

  Background:
    * url baseUrl
    * path '/weight-calculation-rules'
    * def requestJson = read('json/request.json')
    * multipart field data = requestJson
    * method POST
    * def id = response.data.id

    * def uuid = function() {return java.util.UUID.randomUUID().toString() }

  @WeightCalculationRuleGet
  Scenario: get an existing weight calculation rule
    Given path '/weight-calculation-rules/' + id
    When method GET
    Then status 200

    * match response.data.id == id

  @WeightCalculationRuleGet
  Scenario: get a non-existing weight calculation rule
    Given def unknownId = uuid()
    And path '/weight-calculation-rules/' + unknownId
    When method GET
    Then status 404

    * match response.apierror.message == `Weight Calculation Rule with Id ${unknownId} not found`