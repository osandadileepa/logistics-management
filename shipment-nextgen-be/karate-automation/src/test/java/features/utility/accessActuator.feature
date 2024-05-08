Feature: Access actuator endpoints

  Background:
    * url baseUrl

  @Actuator
  @Utility
  Scenario: Access Actuator with valid token
    * def bearer = token
    Given path '/actuator/health'
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

  @Actuator
  @Utility
  Scenario: Access Actuator with invalid token
    * def bearer = 'invalid token'
    Given path '/actuator/health'
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 401