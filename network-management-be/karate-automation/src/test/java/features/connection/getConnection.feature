Feature: Get Connection

  Background:
    * url baseUrl
    * def bearer = token

    * def createReq = call read('classpath:features/connection/createConnection.feature')
    * def connectionId = createReq.response.data.id

  @Connections @Regression
  Scenario: Get Connection
    Given path '/connections/' + connectionId
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def data = response.data
    * match data.id == '#present'
    * match data.id == '#notnull'
    * match data.id == connectionId

