# By default, Contract testing with 3rd Party client should be disabled or ignored since it should not be dependent on the client.
# Hence it should be run manually, Just remove the annotation to test.
Feature: Retrieve Drivers on QPortal

  Background:
    * url baseUrl
    * def bearer = token
    * def organization_id = '750edba3-b7d8-4ecb-b743-80bf4848ab70'
    * print organization_id

  @ignore
  @QPortalListDrivers
  Scenario: Retrieve Drivers
    Given url utils.decodeUrl(baseUrl + '/qportal/drivers?organizationId=' + organization_id)
    And header Authorization = 'Bearer ' + bearer
    When method GET
    Then status 200

    * def response = $
    * print response
    * match response.data == '#present'