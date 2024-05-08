Feature: Delete Node

  Background:
    * url baseUrl
    * def bearer = token

    * def createReq = call read('classpath:features/node/createNode.feature')
    * def nodeId = createReq.response.data.id

  @Nodes @Regression
  Scenario: Delete Node
    Given path '/nodes/' + nodeId
    And header Authorization = 'Bearer '+ bearer
    When method DELETE
    Then status 200

    Given path '/nodes/' + nodeId
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 404
