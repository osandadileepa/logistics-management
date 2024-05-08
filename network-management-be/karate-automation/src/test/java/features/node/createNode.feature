Feature: Create Node

  Background:
    * url baseUrl
    * def bearer = token
    
    * def req = read('classpath:features/node/json/request.json')
    * req.data.node_code = utils.uuid().substring(0, 10)

  @Nodes @Regression
  Scenario: Create Node
    Given path '/nodes/'
    And header Authorization = 'Bearer '+ bearer
    And request req
    When method POST
    Then status 201

    * def data = response.data
    * match data.id == '#present'
    * match data.id == '#notnull'
    * match data.facility.lat == '#notnull'
    * match data.facility.lon == '#notnull'
    * match data.capacity_profile == '#present'
    * match data.capacity_profile.max_weight == '#notnull'
    * match data.measurement_units == '#present'
    * match data.measurement_units.weight_unit == '#notnull'
