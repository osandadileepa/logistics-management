Feature: Shipments > Edit Permission

  Background:
    * url baseUrl
    * def bearer = token

    * def userCredentials = 'classpath:session/json/shipments_edit.json'
    * def newSession = callonce read('classpath:session/create.feature') {userCredentials : '#(userCredentials)'}
    * def newToken = newSession.response.data.token

    * def updateRequest = read('classpath:features/shipment/json/updateRQ.json')
    * updateRequest.data.id = utils.uuid().substring(0, 10)

    * def exportRequest = read('classpath:features/shipment/json/exportRQ.json')
    * exportRequest.data.shipment_tracking_id = utils.uuid().substring(0, 10)

  @Permissions
  Scenario: Happy path: Shipment Editor retrieves a shipment
    Given path '/shipments/' + utils.uuid().substring(0, 10)
    And header Authorization = 'Bearer ' + newToken
    When method GET
    Then status 404

  @Permissions
  Scenario: Happy path: Shipment Editor updates a shipment
    Given path '/shipments'
    And header Authorization = 'Bearer '+ newToken
    And request updateRequest
    When method PUT
    Then status 404

  @Permissions
  Scenario: Unhappy path: Shipment Editor exports a shipment
    Given path '/shipments/export'
    And header Authorization = 'Bearer '+ newToken
    And request exportRequest
    When method POST
    Then status 401