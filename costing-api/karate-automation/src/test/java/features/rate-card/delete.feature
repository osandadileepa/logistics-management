Feature: Delete Rate Card

  Background:
    * url baseUrl
    * path '/rate-cards/'
    * def createRequest = read('json/request.json')
    * request createRequest
    * method POST
    * def id = response.data.id

  @RateCardDelete
  Scenario: delete an existing rate card
    Given path '/rate-cards/' + id
    When method DELETE
    Then status 200