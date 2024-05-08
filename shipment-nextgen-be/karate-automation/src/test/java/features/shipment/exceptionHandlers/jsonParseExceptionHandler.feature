Feature: Handle JSON Parse Exception

  Background:
    * url baseUrl
    * def requestPath = karate.get('singleRQPath', 'classpath:features/shipment/json/single/invalidJsonRequest.json')
    * def invalidJsonRequest = read(requestPath)
    * print invalidJsonRequest


  @ExceptionHandler
  @Regression
  Scenario: Return Bad Request (400) when request causes Json Parse Exception
    * def bearer = token
    Given path '/shipments'
    And header Authorization = 'Bearer '+ bearer
    And request invalidJsonRequest
    When method POST
    Then status 400

    * def response = $
    * print response
