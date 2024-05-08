Feature: Get All Cost

  Background:
    * url baseUrl
    * def bearer = token

    * def createReq = callonce read('classpath:features/cost/create/createCost.feature')

    * def searchRequest = read('classpath:features/cost/json/searchCostRQ.json')
    * remove searchRequest.data.incurred_bys
    * searchRequest.data.incurred_date_range.incurred_date_to = utils.getFormattedOffsetDateTimeNow().substring(0,19)
    * searchRequest.data.cost_amount_range.max_cost_amount = 5000

    * def pasayUserCredentials = 'classpath:session/json/pasay_location_coverage_user.json'
    * def pasaySession = callonce read('classpath:session/create.feature') {userCredentials : '#(pasayUserCredentials)'}
    * def pasayToken = pasaySession.response.data.token

  @CostGetAll @Regression
  Scenario: Get All Cost
    Given url baseUrl + '/costs/list'
    And header Authorization = 'Bearer '+ bearer
    And request searchRequest
    When method POST
    Then status 200

    * def data = response.data
    * match data.result[0].id == '#present'
    * match data.result[0].id == '#notnull'
    * match data.result[0].cost_type == '#notnull'
    * match data.result[0].cost_amount == '#notnull'
    * match data.result[0].currency_code == '#notnull'
    * match data.result[0].currency_symbol == '#notnull'
    * match data.result[0].incurred_by == '#notnull'
    * match data.result[0].incurred_by_date == '#notnull'
    * match data.result[0].incurred_by_timezone == '#notnull'

  @CostGetAll @Regression
  Scenario: Get All Cost without pasay coverage
    * def bearer = pasayToken
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