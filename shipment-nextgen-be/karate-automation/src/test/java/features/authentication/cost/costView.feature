Feature: Costs > View Permission

  Background:
    * url baseUrl
    * def bearer = token

    * def userCredentials = 'classpath:session/json/cost_view.json'
    * def newSession = callonce read('classpath:session/create.feature') {userCredentials : '#(userCredentials)'}
    * def newToken = newSession.response.data.token

    * def requestBody = read('classpath:features/cost/json/costRQ.json')
    * requestBody.data.cost_type.id = utils.uuid()
    * requestBody.data.currency.id = utils.uuid()
    * requestBody.data.driver_id = utils.uuid()
    * requestBody.data.shipments[0].id = utils.uuid()
    * requestBody.data.shipments[0].segments[0].segment_id = utils.uuid()

    * def searchRequest = read('classpath:features/cost/json/searchCostRQ.json')

  @CostPermissions
  Scenario: Happy path: Cost Viewer retrieves a cost
    Given path '/costs/' + utils.uuid()
    And header Authorization = 'Bearer ' + newToken
    When method GET
    Then status 404

  @CostPermissions
  Scenario: Happy path: Cost Viewer retrieves cost listing
    Given path '/costs/list'
    And header Authorization = 'Bearer ' + newToken
    And request searchRequest
    When method POST
    Then status 200

  @CostPermissions
  Scenario: Unhappy path: Cost Viewer creates a cost
    Given path '/costs'
    And header Authorization = 'Bearer '+ newToken
    And request requestBody
    When method POST
    Then status 401

  @CostPermissions
  Scenario: Unhappy path: Cost Viewer updates a cost
    Given path '/costs/' + utils.uuid()
    And header Authorization = 'Bearer '+ newToken
    And request requestBody
    When method PUT
    Then status 401

  @CostPermissions
  Scenario: Unhappy path: Cost Viewer calls shipment selection
    Given path '/costs/shipments/search'
    And header Authorization = 'Bearer ' + newToken
    And request read('classpath:features/shipment/json/search/keysTrackingIdsRQ.json')
    When method POST
    Then status 401