Feature: Get Node

  Background:
    * url baseUrl
    * def bearer = token

    * def createReq = call read('classpath:features/node/createNode.feature')
    * def nodeId = createReq.response.data.id

  @Nodes @Regression
  Scenario: Get Node
    Given path '/nodes/' + nodeId
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def data = response.data
    * match data.id == '#present'
    * match data.id == '#notnull'
    * match data.id == nodeId

