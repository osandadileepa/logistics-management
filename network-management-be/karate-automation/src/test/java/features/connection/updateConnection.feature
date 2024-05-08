Feature: Update Connection

  Background:
    * url baseUrl
    * def bearer = token

    * def createReq = call read('classpath:features/connection/createConnection.feature')
    * def connectionId = createReq.response.data.id

    * def updateReq = createReq.response
    * updateReq.data.id = null
    * updateReq.data.cost = 300

  @Connections @Regression
  Scenario: Update Connection
    Given path '/connections/' + connectionId
    And header Authorization = 'Bearer '+ bearer
    And request updateReq
    When method PUT
    Then status 200

    * def data = response.data
    * match data.id == '#present'
    * match data.id == '#notnull'
    * match data.id == connectionId
    * match data.cost == 300