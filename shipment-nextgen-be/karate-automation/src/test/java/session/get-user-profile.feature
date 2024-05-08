Feature: Get Current User Profile

  Background:
    * url qPortalUrl
    * def jwtToken = karate.get('jwtToken', null)

  Scenario: Create Current User Profile
    Given path '/api/v1/users/get_my_profile'
    And header Authorization = 'Bearer ' + jwtToken
    When method GET
    Then status 200