Feature: Update Rate Card

  Background:
    * url baseUrl
    * path '/rate-cards/'
    * def createRequest = read('json/request.json')
    * request createRequest
    * method POST
    * def id = response.data.id

    * path '/rate-cards/' + id
    * def updateRequest = createRequest

  @RateCardUpdate
  Scenario: update an existing rate card
    Given updateRequest.data.calculation_type = 'PER_WEIGHT_UNIT'
    And request updateRequest
    When method PUT
    Then status 200

    * match response.data.id == id
    * match response.data.calculation_type == 'PER_WEIGHT_UNIT'

  @RateCardUpdate
  Scenario: a required field is missing
    Given updateRequest.data.calculation_type = null
    And request updateRequest
    When method PUT
    Then status 400

    * match response.apierror.errors contains deep 'calculationType must not be null'

  @RateCardUpdate
  Scenario: min is above max
    Given updateRequest.data.min = 201
    And request updateRequest
    When method PUT
    Then status 400

    * match response.apierror.errors contains deep 'min should not be greater than max'