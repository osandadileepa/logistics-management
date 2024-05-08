Feature: Create Session

  Background:
    * url baseUrl
    * def credentials = karate.get('userCredentials', 'classpath:session/json/karate_user.json')
    * def authUser = read(credentials)

  Scenario: Create Session
    * def shipment =
      """
      {
        "shipmentTrackingID": "one-hundred"
      }
      """
    Given path '/sessions'
    And request authUser
    When method POST
    Then status 200
    * def token = response.data.token

