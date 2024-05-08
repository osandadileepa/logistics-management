Feature: Costs > Edit Permission

  Background:
    * url baseUrl
    * def bearer = token

    * def userCredentials = 'classpath:session/json/cost_edit.json'
    * def newSession = callonce read('classpath:session/create.feature') {userCredentials : '#(userCredentials)'}
    * def newToken = newSession.response.data.token

    * def requestBody = read('classpath:features/cost/json/costRQ.json')
    * requestBody.data.cost_type.id = utils.uuid().substring(0, 10)
    * requestBody.data.currency.id = utils.uuid().substring(0, 10)
    * requestBody.data.driver_id = utils.uuid().substring(0, 10)
    * requestBody.data.shipments[0].id = utils.uuid().substring(0, 10)
    * requestBody.data.shipments[0].segments[0].segment_id = utils.uuid().substring(0, 10)

  @CostPermissions
  Scenario: Happy path: Cost Editor retrieves a cost
    Given path '/costs/' + utils.uuid().substring(0, 10)
    And header Authorization = 'Bearer ' + newToken
    When method GET
    Then status 404

  @CostPermissions
  Scenario: Happy path: Cost Editor retrieves cost listing
    Given path '/costs'
    And header Authorization = 'Bearer ' + newToken
    When method GET
    Then status 200

  @CostPermissions
  Scenario: Unhappy path: Cost Editor creates a cost
    Given path '/costs'
    And header Authorization = 'Bearer '+ newToken
    And request requestBody
    When method POST
    Then status 401

  @CostPermissions
  Scenario: Happy path: Cost Editor updates a cost
    Given path '/costs/' + utils.uuid().substring(0, 10)
    And header Authorization = 'Bearer '+ newToken
    And request requestBody
    When method PUT
    Then status 404

  @CostPermissions
  Scenario: Happy path: Cost Editor calls shipment selection
    Given path '/costs/shipments/search'
    And header Authorization = 'Bearer ' + newToken
    When method GET
    Then status 200