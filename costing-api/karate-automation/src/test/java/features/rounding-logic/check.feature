Feature: Rounding Logic

  Background:
    * url baseUrl
    * def requestJson = read('json/request.json')
    * path '/rounding-logic'

  @RoundingLogic
  Scenario: round to 1's
    Given request requestJson
    When method POST
    Then status 200

    * match response.data.result == 12346

  @RoundingLogic
  Scenario: round to 10's
    Given requestJson.data.rounding_logic.round_to = 10
    And requestJson.data.rounding_logic.threshold = 5
    And request requestJson
    When method POST
    Then status 200

    * match response.data.result == 12350

  @RoundingLogic
  Scenario: round to 100's
    Given requestJson.data.rounding_logic.round_to = 100
    And requestJson.data.rounding_logic.threshold = 50
    And request requestJson
    When method POST
    Then status 200

    * match response.data.result == 12300

  @RoundingLogic
  Scenario: round to 1000's
    Given requestJson.data.rounding_logic.round_to = 1000
    And requestJson.data.rounding_logic.threshold = 500
    And request requestJson
    When method POST
    Then status 200

    * match response.data.result == 12000

  @RoundingLogic
  Scenario: round to 0.001's
    Given requestJson.data.rounding_logic.round_to = 0.001
    And requestJson.data.rounding_logic.threshold = 0.0005
    And request requestJson
    When method POST
    Then status 200

    * match response.data.result == 12345.679

  @RoundingLogic
  Scenario: round to 0.01's
    Given requestJson.data.rounding_logic.round_to = 0.01
    And requestJson.data.rounding_logic.threshold = 0.005
    And request requestJson
    When method POST
    Then status 200

    * match response.data.result == 12345.68

  @RoundingLogic
  Scenario: round to 0.1's
    Given requestJson.data.rounding_logic.round_to = 0.1
    And requestJson.data.rounding_logic.threshold = 0.05
    And request requestJson
    When method POST
    Then status 200

    * match response.data.result == 12345.7

  @RoundingLogic
  Scenario: round up if above threshold
    Given requestJson.data.value = 777.7
    And request requestJson
    When method POST
    Then status 200

    * match response.data.result == 778

  @RoundingLogic
  Scenario: round down if below threshold
    Given requestJson.data.rounding_logic.threshold = 0.8
    And requestJson.data.value = 777.7
    And request requestJson
    When method POST
    Then status 200

    * match response.data.result == 777

  @RoundingLogic
  Scenario: invalid roundTo
    Given requestJson.data.rounding_logic.round_to = 2
    And request requestJson
    When method POST
    Then status 400

    * match response.apierror.errors contains deep 'roundingLogic error: invalid roundTo value'

  @RoundingLogic
  Scenario: invalid threshold
    Given requestJson.data.rounding_logic.threshold = 1
    And request requestJson
    When method POST
    Then status 400

    * match response.apierror.errors contains deep 'roundingLogic error: invalid threshold value'