#By default, Contract testing with 3rd Party client should be disabled or ignored since it should not be dependent on the client.
# Hence it should be run manually, Just remove the annotation to test.
Feature: Retrieve Vehicle Types on QPortal

  Background:
    * url baseUrl
    * def bearer = token

  @ignore
  @QPortalListVehicleTypes
  Scenario: Retrieve Vehicle Types
    Given url utils.decodeUrl(baseUrl + '/qportal/vehicle-types')
    And header Authorization = 'Bearer ' + bearer
    When method GET
    Then status 200

    * def response = $
    * match response.data == '#present'