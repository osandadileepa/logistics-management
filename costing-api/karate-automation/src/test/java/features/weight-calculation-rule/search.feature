Feature: Get Weight Calculation Rule

  Background:
    * url baseUrl
    * def requestJson = read('json/request.json')
    * def uuid = function() {return java.util.UUID.randomUUID().toString() }

    * def orgId_1 = uuid()
    * def partner_1 = read('json/partner.json')
    * def partner_2 = read('json/partner.json')
    * partner_1.id = uuid()
    * partner_2.id = uuid()

    * requestJson.data.organization_id = orgId_1
    * requestJson.data.active = true

    * path '/weight-calculation-rules'
    * requestJson.data.partners = []
    * multipart field data = requestJson
    * method POST

    * path '/weight-calculation-rules'
    * requestJson.data.partners = [partner_1]
    * multipart field data = requestJson
    * method POST

    * path '/weight-calculation-rules'
    * requestJson.data.partners = [partner_2]
    * multipart field data = requestJson
    * method POST

    * path '/weight-calculation-rules/search'

  @WeightCalculationRuleSearch
  Scenario: search by organization id
    Given param organizationId = orgId_1
    When method GET
    Then status 200

    * match response.data.result != []
    * match response.data.size == 10
    * match response.data.page == 0
    * match response.data.total_elements == 2
    * match response.data.total_pages == 1

  @WeightCalculationRuleSearch
  Scenario: search by organization id including default rules
    Given param organizationId = orgId_1
    And param includeDefaultRules = true
    When method GET
    Then status 200

    * match response.data.result != []
    * match response.data.size == 10
    * match response.data.page == 0
    * match response.data.total_elements == 3
    * match response.data.total_pages == 1

  @WeightCalculationRuleSearch
  Scenario: search unknown organization id
    Given param organizationId = uuid()
    When method GET
    Then status 200

    * match response.data.result == []
    * match response.data.size == 10
    * match response.data.page == 0
    * match response.data.total_elements == 0
    * match response.data.total_pages == 0