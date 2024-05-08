Feature: Create Connection AIR

  Background:
    * url baseUrl
    * def bearer = token

    * def listVendors = call read('classpath:features/qportal/listVendors.feature')
    * def listCurrencies = call read('classpath:features/qportal/listCurrencies.feature')
    * def listVehicleTypes = call read('classpath:features/qportal/listVehicleTypes.feature')
    * def departureNode = call read('classpath:features/node/createNode.feature')
    * def arrivalNode = call read('classpath:features/node/createNode.feature')

    * def req = read('classpath:features/connection/json/request.json')
    * req.data.vendor.id = listVendors.response.data[0].id
    * req.data.currency.id = listCurrencies.response.data[0].id
    * req.data.vehicle_type.id = listVehicleTypes.response.data[0].id
    * req.data.departure_node.id = departureNode.response.data.id
    * req.data.arrival_node.id = arrivalNode.response.data.id
    * req.data.connection_code = utils.uuid().substring(0, 10)
    * req.data.transport_type = 'AIR'
    * req.data.air_lockout_duration = 10
    * req.data.air_recovery_duration = 15
    * req.data.vehicle_type == null

  @Connections @Test @Regression
  Scenario: Create Connection
    Given path '/connections/'
    And header Authorization = 'Bearer '+ bearer
    And request req
    When method POST
    Then status 201

    * def data = response.data
    * match data.id == '#present'
    * match data.id == '#notnull'
    * match data.vehicle_type == '#notnull'
    * match data.vehicle_type.name == '#present'
