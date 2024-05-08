Feature: Update a Weight Calculation Rule

  Background:
    * url baseUrl
    * path '/weight-calculation-rules'
    * def requestJson = read('json/request.json')
    * multipart field data = requestJson
    * method POST
    * def id = response.data.id
    * path '/weight-calculation-rules/' + id

  @WeightCalculationRuleUpdate
  Scenario: update an existing weight calculation rule
    Given requestJson.data.name = 'Automation Rule Modified'
    And multipart field data = requestJson
    When method PUT
    Then status 200

    * match response.data.id == id
    * match response.data.name == 'Automation Rule Modified'

  @WeightCalculationRuleUpdate
  Scenario: volumeWeightRule is null when chargeableWeightRule is ALWAYS_PICK_ACTUAL_WEIGHT
    Given requestJson.data.volume_weight_rule = null
    And requestJson.data.chargeable_weight_rule = 'ALWAYS_PICK_ACTUAL_WEIGHT'
    And multipart field data = requestJson
    When method PUT
    Then status 200

  @WeightCalculationRuleUpdate
  Scenario: attached specialVolumeWeightTemplate is valid
    Given requestJson.data.volume_weight_rule = 'SPECIAL'
    And multipart file file = { read: 'template/csvTemplate.csv', contentType: 'text/csv' }
    And multipart field data = requestJson
    When method PUT
    Then status 200

    * match response.data.id == '#present'
    * match response.data.id == '#notnull'
    * def expectedSpecialVolumeWeightRule = read('json/specialVolumeWeightRule.json')
    * match response.data.special_volume_weight_rule == expectedSpecialVolumeWeightRule

  @WeightCalculationRuleUpdate
  Scenario: attached specialVolumeWeightTemplate has no conversion table
    Given requestJson.data.volume_weight_rule = 'SPECIAL'
    And multipart file file = { read: 'template/noConversionTable.csv', contentType: 'text/csv' }
    And multipart field data = requestJson
    When method PUT
    Then status 200

    * match response.data.special_volume_weight_rule.custom_formula == 'l+w+h'
    * match response.data.special_volume_weight_rule.conversions == []

  @WeightCalculationRuleUpdate
  Scenario: a required field is missing
    Given requestJson.data.chargeable_weight_rule = null
    And multipart field data = requestJson
    When method PUT
    Then status 400

    * match response.apierror.errors contains deep 'chargeableWeightRule must not be null'

  @WeightCalculationRuleUpdate
  Scenario: volumeWeightRule is null when chargeableWeightRule is ALWAYS_PICK_VOLUME_WEIGHT
    Given requestJson.data.volume_weight_rule = null
    And requestJson.data.chargeable_weight_rule = 'ALWAYS_PICK_VOLUME_WEIGHT'
    And multipart field data = requestJson
    When method PUT
    Then status 400

    * match response.apierror.errors contains deep 'volumeWeightRule must be set due to given chargeableWeightRule'

  @WeightCalculationRuleUpdate
  Scenario: volumeWeightRule is null when chargeableWeightRule is HIGHER_VALUE_BETWEEN_ACTUAL_AND_VOLUME_WEIGHT
    Given requestJson.data.volume_weight_rule = null
    And requestJson.data.chargeable_weight_rule = 'HIGHER_VALUE_BETWEEN_ACTUAL_AND_VOLUME_WEIGHT'
    And multipart field data = requestJson
    When method PUT
    Then status 400

    * match response.apierror.errors contains deep 'volumeWeightRule must be set due to given chargeableWeightRule'

  @WeightCalculationRuleUpdate
  Scenario: standardVolumeWeightRuleDivisor is null when volumeWeightRule is STANDARD
    Given requestJson.data.standard_volume_weight_rule_divisor = null
    And requestJson.data.volume_weight_rule = 'STANDARD'
    And multipart field data = requestJson
    When method PUT
    Then status 400

    * match response.apierror.errors contains deep 'standardVolumeWeightRuleDivisor is required when volumeWeightRule is set to STANDARD'

  @WeightCalculationRuleUpdate
  Scenario: standardVolumeWeightRuleDivisor is null when volumeWeightRule is STANDARD
    Given requestJson.data.standard_volume_weight_rule_divisor = 0
    And multipart field data = requestJson
    When method PUT
    Then status 400

    * match response.apierror.errors contains deep 'standardVolumeWeightRuleDivisor should not be set to 0'

  @WeightCalculationRuleUpdate
  Scenario: actualWeightMin is greater than actualWeightMax
    Given requestJson.data.actual_weight_min = 1
    And requestJson.data.actual_weight_max = 0
    And multipart field data = requestJson
    When method PUT
    Then status 400

    * match response.apierror.errors contains deep 'actualWeightMin should not be greater than actualWeightMax'

  @WeightCalculationRuleUpdate
  Scenario: volumeWeightMin is greater than volumeWeightMax
    Given requestJson.data.volume_weight_min = 1
    And requestJson.data.volume_weight_max = 0
    And multipart field data = requestJson
    When method PUT
    Then status 400

    * match response.apierror.errors contains deep 'volumeWeightMin should not be greater than volumeWeightMax'

  @WeightCalculationRuleUpdate
  Scenario: chargeableWeightMin is greater than chargeableWeightMax
    Given requestJson.data.chargeable_weight_min = 1
    And requestJson.data.chargeable_weight_max = 0
    And multipart field data = requestJson
    When method PUT
    Then status 400

    * match response.apierror.errors contains deep 'chargeableWeightMin should not be greater than chargeableWeightMax'

  @WeightCalculationRuleUpdate
  Scenario: no attached specialVolumeWeightTemplate when volumeWeightRule is SPECIAL
    Given requestJson.data.volume_weight_rule = 'SPECIAL'
    And multipart field data = requestJson
    When method PUT
    Then status 400

    * match response.apierror.message == 'No attached specialVolumeWeightTemplate file found'

  @WeightCalculationRuleUpdate
  Scenario: attached specialVolumeWeightTemplate is not a valid file format
    Given requestJson.data.volume_weight_rule = 'SPECIAL'
    And multipart field data = requestJson
    And multipart file file = { read: 'template/txtTemplate.txt'}
    When method PUT
    Then status 400

    * match response.apierror.errors contains deep 'The file uploaded file should be in either .CSV, .XLS or .XLSX format'

  @WeightCalculationRuleUpdate
  Scenario: attached specialVolumeWeightTemplate has invalid formula
    Given requestJson.data.volume_weight_rule = 'SPECIAL'
    And multipart file file = { read: 'template/invalidFormula.csv', contentType: 'text/csv' }
    And multipart field data = requestJson
    When method PUT
    Then status 400

    * match response.apierror.errors contains deep 'The custom formula specified in the template is invalid'

  @WeightCalculationRuleUpdate
  Scenario: attached specialVolumeWeightTemplate has an invalid row
    Given requestJson.data.volume_weight_rule = 'SPECIAL'
    And multipart file file = { read: 'template/invalidRow.csv', contentType: 'text/csv' }
    And multipart field data = requestJson
    When method PUT
    Then status 400

    * match response.apierror.errors contains deep 'Error parsing CSV line: 6. [40.0,90.0,FAIL,]'

  @WeightCalculationRuleUpdate
  Scenario: attached specialVolumeWeightTemplate has an invalid conversion table
    Given requestJson.data.volume_weight_rule = 'SPECIAL'
    And multipart file file = { read: 'template/invalidConversionTable.csv', contentType: 'text/csv' }
    And multipart field data = requestJson
    When method PUT
    Then status 400

    * match response.apierror.errors contains deep 'There is a problem with the conversion table specified in the template'

  @WeightCalculationRuleUpdate
  Scenario: no organization id
    Given requestJson.data.organization_id = null
    And multipart field data = requestJson
    When method PUT
    Then status 400

    * match response.apierror.errors contains deep 'organizationId must not be null'

  @WeightCalculationRuleUpdate
  Scenario: partner id is blank
    Given requestJson.data.partners[0].id = ""
    And multipart field data = requestJson
    When method PUT
    Then status 400

    * match response.apierror.errors contains deep 'partners[].id must not be blank'

  @WeightCalculationRuleUpdate
  Scenario: partner id is null
    Given requestJson.data.partners[0].id = null
    And multipart field data = requestJson
    When method PUT
    Then status 400

    * match response.apierror.errors contains deep 'partners[].id must not be null'

  @WeightCalculationRuleUpdate
  Scenario: partner name is blank
    Given requestJson.data.partners[0].name = ""
    And multipart field data = requestJson
    When method PUT
    Then status 400

    * match response.apierror.errors contains deep 'partners[].name must not be blank'

  @WeightCalculationRuleUpdate
  Scenario: partner name is null
    Given requestJson.data.partners[0].name = null
    And multipart field data = requestJson
    When method PUT
    Then status 400

    * match response.apierror.errors contains deep 'partners[].name must not be null'

  @WeightCalculationRuleUpdate
  Scenario: invalid roundTo
    Given requestJson.data.rounding_logic.round_to = 2
    And multipart field data = requestJson
    When method PUT
    Then status 400

    * match response.apierror.errors contains deep 'roundingLogic error: invalid roundTo value'

  @WeightCalculationRuleUpdate
  Scenario: invalid threshold
    Given requestJson.data.rounding_logic.threshold = 1
    And multipart field data = requestJson
    When method PUT
    Then status 400

    * match response.apierror.errors contains deep 'roundingLogic error: invalid threshold value'