Feature: Create Rate Card

  Background:
    * url baseUrl
    * path '/rate-cards'
    * def createRequest = read('json/request.json')

  @RateCardCreate
  Scenario: create a simple rate card
    Given request createRequest
    When method POST
    Then status 200

    * match response.data.id == '#present'
    * match response.data.id == '#notnull'

  @RateCardCreate
  Scenario: a required field is missing
    Given createRequest.data.calculation_type = null
    And request createRequest
    When method POST
    Then status 400

    * match response.apierror.errors contains deep 'calculationType must not be null'

  @RateCardCreate
  Scenario: min is above max
    Given createRequest.data.min = 201
    And request createRequest
    When method POST
    Then status 400

    * match response.apierror.errors contains deep 'min should not be greater than max'