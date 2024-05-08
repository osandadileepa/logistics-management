# By default, Contract testing with 3rd Party client should be disabled or ignored since it should not be dependent on the client.
# Hence it should be run manually, Just remove the annotation to test.
Feature: Retrieve Currencies from QPortal

  Background:
    * url baseUrl
    * def bearer = token

  @ignore
  @QPortal
  Scenario: Retrieve Currencies from QPortal
    Given url utils.decodeUrl(baseUrl + '/qportal/currencies')
    And header Authorization = 'Bearer ' + bearer
    When method GET
    Then status 200

    * def response = $
    * print response
    * match response.data == '#present'