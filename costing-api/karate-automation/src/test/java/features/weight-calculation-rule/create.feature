Feature: Create Weight Calculation Rule

  Background:
    * url baseUrl
    * path '/weight-calculation-rules'
    * def requestJson = read('json/request.json')

  @WeightCalculationRuleCreate
  Scenario: create a simple weight calculation rule
    Given multipart field data = requestJson
    When method POST
    Then status 200

    * match response.data.id == '#present'
    * match response.data.id == '#notnull'

  @WeightCalculationRuleCreate
  Scenario: volumeWeightRule is null when chargeableWeightRule is ALWAYS_PICK_ACTUAL_WEIGHT
    Given requestJson.data.volume_weight_rule = null
    And requestJson.data.chargeable_weight_rule = 'ALWAYS_PICK_ACTUAL_WEIGHT'
    And multipart field data = requestJson
    When method POST
    Then status 200

  @WeightCalculationRuleCreate
  Scenario: attached specialVolumeWeightTemplate is valid
    Given requestJson.data.volume_weight_rule = 'SPECIAL'
    And multipart file file = { read: 'template/csvTemplate.csv', contentType: 'text/csv' }
    And multipart field data = requestJson
    When method POST
    Then status 200

    * match response.data.id == '#present'
    * match response.data.id == '#notnull'
    * def expectedSpecialVolumeWeightRule = read('json/specialVolumeWeightRule.json')
    * match response.data.special_volume_weight_rule == expectedSpecialVolumeWeightRule

  @WeightCalculationRuleCreate
  Scenario: attached specialVolumeWeightTemplate has no conversion table
    Given requestJson.data.volume_weight_rule = 'SPECIAL'
    And multipart file file = { read: 'template/noConversionTable.csv', contentType: 'text/csv' }
    And multipart field data = requestJson
    When method POST
    Then status 200

    * match response.data.special_volume_weight_rule.custom_formula == 'l+w+h'
    * match response.data.special_volume_weight_rule.conversions == []

  @WeightCalculationRuleCreate
  Scenario: a required field is missing
    Given requestJson.data.chargeable_weight_rule = null
    And multipart field data = requestJson
    When method POST
    Then status 400

    * match response.apierror.errors contains deep 'chargeableWeightRule must not be null'

  @WeightCalculationRuleCreate
  Scenario: volumeWeightRule is null when chargeableWeightRule is ALWAYS_PICK_VOLUME_WEIGHT
    Given requestJson.data.volume_weight_rule = null
    And requestJson.data.chargeable_weight_rule = 'ALWAYS_PICK_VOLUME_WEIGHT'
    And multipart field data = requestJson
    When method POST
    Then status 400

    * match response.apierror.errors contains deep 'volumeWeightRule must be set due to given chargeableWeightRule'

  @WeightCalculationRuleCreate
  Scenario: volumeWeightRule is null when chargeableWeightRule is HIGHER_VALUE_BETWEEN_ACTUAL_AND_VOLUME_WEIGHT
    Given requestJson.data.volume_weight_rule = null
    And requestJson.data.chargeable_weight_rule = 'HIGHER_VALUE_BETWEEN_ACTUAL_AND_VOLUME_WEIGHT'
    And multipart field data = requestJson
    When method POST
    Then status 400

    * match response.apierror.errors contains deep 'volumeWeightRule must be set due to given chargeableWeightRule'

  @WeightCalculationRuleCreate
  Scenario: standardVolumeWeightRuleDivisor is null when volumeWeightRule is STANDARD
    Given requestJson.data.standard_volume_weight_rule_divisor = null
    And requestJson.data.volume_weight_rule = 'STANDARD'
    And multipart field data = requestJson
    When method POST
    Then status 400

    * match response.apierror.errors contains deep 'standardVolumeWeightRuleDivisor is required when volumeWeightRule is set to STANDARD'

  @WeightCalculationRuleCreate
  Scenario: standardVolumeWeightRuleDivisor is null when volumeWeightRule is STANDARD
    Given requestJson.data.standard_volume_weight_rule_divisor = 0
    And multipart field data = requestJson
    When method POST
    Then status 400

    * match response.apierror.errors contains deep 'standardVolumeWeightRuleDivisor should not be set to 0'

  @WeightCalculationRuleCreate
  Scenario: actualWeightMin is greater than actualWeightMax
    Given requestJson.data.actual_weight_min = 1
    And requestJson.data.actual_weight_max = 0
    And multipart field data = requestJson
    When method POST
    Then status 400

    * match response.apierror.errors contains deep 'actualWeightMin should not be greater than actualWeightMax'

  @WeightCalculationRuleCreate
  Scenario: volumeWeightMin is greater than volumeWeightMax
    Given requestJson.data.volume_weight_min = 1
    And requestJson.data.volume_weight_max = 0
    And multipart field data = requestJson
    When method POST
    Then status 400

    * match response.apierror.errors contains deep 'volumeWeightMin should not be greater than volumeWeightMax'

  @WeightCalculationRuleCreate
  Scenario: chargeableWeightMin is greater than chargeableWeightMax
    Given requestJson.data.chargeable_weight_min = 1
    And requestJson.data.chargeable_weight_max = 0
    And multipart field data = requestJson
    When method POST
    Then status 400

    * match response.apierror.errors contains deep 'chargeableWeightMin should not be greater than chargeableWeightMax'

  @WeightCalculationRuleCreate
  Scenario: no attached specialVolumeWeightTemplate when volumeWeightRule is SPECIAL
    Given requestJson.data.volume_weight_rule = 'SPECIAL'
    And multipart field data = requestJson
    When method POST
    Then status 400

    * match response.apierror.message == 'No attached specialVolumeWeightTemplate file found'

  @WeightCalculationRuleCreate
  Scenario: attached specialVolumeWeightTemplate is not a valid file format
    Given requestJson.data.volume_weight_rule = 'SPECIAL'
    And multipart field data = requestJson
    And multipart file file = { read: 'template/txtTemplate.txt'}
    When method POST
    Then status 400

    * match response.apierror.errors contains deep 'The file uploaded file should be in either .CSV, .XLS or .XLSX format'

  @WeightCalculationRuleCreate
  Scenario: attached specialVolumeWeightTemplate has invalid formula
    Given requestJson.data.volume_weight_rule = 'SPECIAL'
    And multipart file file = { read: 'template/invalidFormula.csv', contentType: 'text/csv' }
    And multipart field data = requestJson
    When method POST
    Then status 400

    * match response.apierror.errors contains deep 'The custom formula specified in the template is invalid'

  @WeightCalculationRuleCreate
  Scenario: attached specialVolumeWeightTemplate has an invalid row
    Given requestJson.data.volume_weight_rule = 'SPECIAL'
    And multipart file file = { read: 'template/invalidRow.csv', contentType: 'text/csv' }
    And multipart field data = requestJson
    When method POST
    Then status 400

    * match response.apierror.errors contains deep 'Error parsing CSV line: 6. [40.0,90.0,FAIL,]'

  @WeightCalculationRuleCreate
  Scenario: attached specialVolumeWeightTemplate has an invalid conversion table
    Given requestJson.data.volume_weight_rule = 'SPECIAL'
    And multipart file file = { read: 'template/invalidConversionTable.csv', contentType: 'text/csv' }
    And multipart field data = requestJson
    When method POST
    Then status 400

    * match response.apierror.errors contains deep 'There is a problem with the conversion table specified in the template'

  @WeightCalculationRuleCreate
  Scenario: no organization id
    Given requestJson.data.organization_id = null
    And multipart field data = requestJson
    When method POST
    Then status 400

    * match response.apierror.errors contains deep 'organizationId must not be null'

  @WeightCalculationRuleCreate
  Scenario: partner id is blank
    Given requestJson.data.partners[0].id = ""
    And multipart field data = requestJson
    When method POST
    Then status 400

    * match response.apierror.errors contains deep 'partners[].id must not be blank'

  @WeightCalculationRuleCreate
  Scenario: partner id is null
    Given requestJson.data.partners[0].id = null
    And multipart field data = requestJson
    When method POST
    Then status 400

    * match response.apierror.errors contains deep 'partners[].id must not be null'

  @WeightCalculationRuleCreate
  Scenario: partner name is blank
    Given requestJson.data.partners[0].name = ""
    And multipart field data = requestJson
    When method POST
    Then status 400

    * match response.apierror.errors contains deep 'partners[].name must not be blank'

  @WeightCalculationRuleCreate
  Scenario: partner name is null
    Given requestJson.data.partners[0].name = null
    And multipart field data = requestJson
    When method POST
    Then status 400

    * match response.apierror.errors contains deep 'partners[].name must not be null'

  @WeightCalculationRuleCreate
  Scenario: invalid roundTo
    Given requestJson.data.rounding_logic.round_to = 2
    And multipart field data = requestJson
    When method POST
    Then status 400

    * match response.apierror.errors contains deep 'roundingLogic error: invalid roundTo value'

  @WeightCalculationRuleCreate
  Scenario: invalid threshold
    Given requestJson.data.rounding_logic.threshold = 1
    And multipart field data = requestJson
    When method POST
    Then status 400

    * match response.apierror.errors contains deep 'roundingLogic error: invalid threshold value'