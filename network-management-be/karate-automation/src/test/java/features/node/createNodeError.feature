Feature: Create Node Unhappy Scenarios

  Background:
    * url baseUrl
    * def bearer = token

    * def req = read('classpath:features/node/json/request.json')
    * req.data.node_code = utils.uuid().substring(0, 10)

  @Nodes @Regression
  Scenario: Create Node without lat/lon
    Given path '/nodes/'
    # associate a facility without lat/lon information
    And req.data.facility.id = '2d0656d4-9eaa-4e46-9495-9e752330b5a1'
    And header Authorization = 'Bearer '+ bearer
    And request req
    When method POST
    Then status 500

    * def res = response
    * match res contains { message: '#string', code: '#string', timestamp: '#string'}
    * match res.message contains 'Update the facility record in QPortal and retry'
    * match res.code == 'UPSERT_FACILITY_FAILED'