Feature: Find Rate Card

  Background:
    * url baseUrl
    * path '/rate-cards/'
    * def createRequest = read('json/request.json')
    * request createRequest
    * method POST
    * def id = response.data.id

    * def uuid = function() {return java.util.UUID.randomUUID().toString() }

  @RateCardGet
  Scenario: find an existing rate card
    Given path '/rate-cards/' + id
    When method GET
    Then status 200

    * match response.data.id == id

  @RateCardGet
  Scenario: find a non-existing rate card
    Given def unknownId = uuid()
    And path '/rate-cards/' + unknownId
    When method GET
    Then status 404

    * match response.apierror.message == `Rate Card with Id ${unknownId} not found`