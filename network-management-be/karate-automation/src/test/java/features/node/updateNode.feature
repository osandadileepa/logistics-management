Feature: Update Node

  Background:
    * url baseUrl
    * def bearer = token

    * def createReq = call read('classpath:features/node/createNode.feature')
    * def nodeId = createReq.response.data.id

    * def updateReq = createReq.response
    * updateReq.data.id = null
    * updateReq.data.description = 'Updated Description'

  @Nodes @Regression
  Scenario: Update Node
    Given path '/nodes/' + nodeId
    And header Authorization = 'Bearer '+ bearer
    And request updateReq
    When method PUT
    Then status 200

    * def data = response.data
    * match data.id == '#present'
    * match data.id == '#notnull'
    * match data.id == nodeId
    * match data.description == 'Updated Description'