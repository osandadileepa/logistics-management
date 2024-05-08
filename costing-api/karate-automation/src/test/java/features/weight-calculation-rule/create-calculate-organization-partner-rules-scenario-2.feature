Feature: Create rule with organization and partner Scenario 2

  Background:
    * url baseUrl
    * path '/weight-calculation-rules'
    * def requestJson = read('json/request.json')
    * def uuid = function(){ return java.util.UUID.randomUUID().toString() }

    * def orgId_1 = uuid()
    * def partner_1 = read('json/partner.json')
    * def partner_2 = read('json/partner.json')
    * def partner_3 = read('json/partner.json')
    * partner_1.id = uuid()
    * partner_2.id = uuid()
    * partner_3.id = uuid()

    * requestJson.data.organization_id = orgId_1
    * requestJson.data.active = true
    * requestJson.data.partners = []
    * multipart field data = requestJson
    * method POST

    * path '/weight-calculation-rules'

  @WeightCalculationRuleCreate
  Scenario: Duplicate active rules for org_1
    Given requestJson.data.partnerIds = []
    And requestJson.data.organization_id = orgId_1
    And requestJson.data.active = true
    And multipart field data = requestJson
    When method POST
    Then status 400

  @WeightCalculationRuleCreate
  Scenario: Create active rule for multiple partners under existing org_1
    Given requestJson.data.partners = [partner_1, partner_2, partner_3]
    And requestJson.data.organization_id = orgId_1
    And requestJson.data.active = true
    And multipart field data = requestJson
    When method POST
    Then status 200
