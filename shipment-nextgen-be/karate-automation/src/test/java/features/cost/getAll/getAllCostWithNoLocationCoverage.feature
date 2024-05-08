Feature: Get All Cost

  Background:
    * url baseUrl
    * def bearer = token

    * def createReq = callonce read('classpath:features/cost/create/createCost.feature')

    * def searchRequest = read('classpath:features/cost/json/searchCostRQ.json')

    * def userCredentials = 'classpath:session/json/no_location_coverage.json'
    * def newSession = callonce read('classpath:session/create.feature') {userCredentials : '#(userCredentials)'}
    * def newToken = newSession.response.data.token

  @CostGetAll @Regression
  Scenario: Get All Cost
    * def bearer = newToken
    Given url baseUrl + '/costs/list'
    And header Authorization = 'Bearer '+ bearer
    And request searchRequest
    When method POST
    Then status 200

    * def response = $
    * print response

    * def data = response.data

    * assert data.total_elements == 0
    * assert data.total_pages == 0
    * match data.result[0] == '#notpresent'

