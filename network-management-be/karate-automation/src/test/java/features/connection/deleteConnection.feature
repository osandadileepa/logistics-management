Feature: Delete Connection

  Background:
    * url baseUrl
    * def bearer = token

    * def createReq = call read('classpath:features/connection/createConnection.feature')
    * def connectionId = createReq.response.data.id

  @Connections @Regression
  Scenario: Delete Connection
    Given path '/connections/' + connectionId
    And header Authorization = 'Bearer '+ bearer
    When method DELETE
    Then status 200

    Given path '/connections/' + connectionId
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 404
