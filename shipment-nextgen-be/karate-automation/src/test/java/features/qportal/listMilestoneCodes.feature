# By default, Contract testing with 3rd Party client should be disabled or ignored since it should not be dependent on the client.
# Hence it should be run manually, Just remove the annotation to test.
Feature: Retrieve Milestone Codes from QPortal

  Background:
    * url baseUrl
    * def bearer = token
    * def organization_id = '9655995f-c682-44e4-9ebd-888a404b7b15'
    * print organization_id

  @ignore
  @QPortalListMilestoneCodes
  Scenario: Retrieve Milestone codes
    Given url utils.decodeUrl(baseUrl + '/qportal/milestone-codes?organizationId=' + organization_id)
    And header Authorization = 'Bearer ' + bearer
    When method GET
    Then status 200

    * def response = $
    * print response
    * match response.data == '#present'

  @ignore
  @QPortalListMilestoneCodes
  Scenario: Retrieve Milestone codes with invalid organization id
    * def organization_id = 'INVALID_ORG'
    Given url utils.decodeUrl(baseUrl + '/qportal/milestone-codes?organizationId=' + organization_id)
    And header Authorization = 'Bearer ' + bearer
    When method GET
    Then status 404