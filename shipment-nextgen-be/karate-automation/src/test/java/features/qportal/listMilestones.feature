# By default, Contract testing with 3rd Party client should be disabled or ignored since it should not be dependent on the client.
# Hence it should be run manually, Just remove the annotation to test.
Feature: Retrieve Milestones on QPortal v2

  Background:
    * url baseUrl
    * def bearer = token


  @QPortalListMilestonesV2
  Scenario: Retrieve Milestones no pagination provided
    Given path '/qportal/v2/milestones'
    And header Authorization = 'Bearer ' + bearer
    When method GET
    Then status 200

    * def response = $
    * print response
    * match response.data == '#present'
    * assert response.data.length > 0


  @QPortalListMilestonesV2
  Scenario: Retrieve Milestones with pagination
    Given url utils.decodeUrl(baseUrl + '/qportal/v2/milestones?page=1&per_page=20')
    And header Authorization = 'Bearer ' + bearer
    When method GET
    Then status 200

    * def response = $
    * print response
    * match response.data == '#present'
    * assert response.data.length == 20