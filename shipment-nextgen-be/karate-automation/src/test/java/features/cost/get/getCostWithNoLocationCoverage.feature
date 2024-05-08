Feature: Get Cost

  Background:
    * url baseUrl
    * def bearer = token

    * def createReq = callonce read('classpath:features/cost/create/createCost.feature')
    * def costId = createReq.response.data.id

    * def userCredentials = 'classpath:session/json/no_location_coverage.json'
    * def newSession = callonce read('classpath:session/create.feature') {userCredentials : '#(userCredentials)'}
    * def newToken = newSession.response.data.token

  @CostGet @Regression
  Scenario: Get Cost
    * def bearer = newToken
    Given path '/costs/' + costId
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 403

    * def data = response.data