Feature: Update Cost

  Background:
    * url baseUrl
    * def bearer = token

    * def createReq = callonce read('classpath:features/cost/create/createCost.feature')
    * def costId = createReq.response.data.id

    * def updateReqBody = createReq.response
    * updateReqBody.data.id = null
    * updateReqBody.data.cost_amount = 4000
    * updateReqBody.data.remarks = 'Updated remarks'

    * def userCredentials = 'classpath:session/json/no_location_coverage.json'
    * def newSession = callonce read('classpath:session/create.feature') {userCredentials : '#(userCredentials)'}
    * def newToken = newSession.response.data.token

  @CostGet @Regression
  Scenario: Update Cost
    * def bearer = newToken
    Given path '/costs/' + costId
    And header Authorization = 'Bearer '+ bearer
    And request updateReqBody
    When method PUT
    Then status 403