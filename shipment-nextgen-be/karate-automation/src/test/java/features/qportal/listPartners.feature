# By default, Contract testing with 3rd Party client should be disabled or ignored since it should not be dependent on the client.
# Hence it should be run manually, Just remove the annotation to test.
Feature: Retrieve Partners on QPortal v2

  Background:
    * url baseUrl
    * def bearer = token

    * def userWithoutPartner = 'classpath:session/json/company_user_no_partner.json'
    * def newSessionForUserWithoutPartner = callonce read('classpath:session/create.feature') {userCredentials : '#(userWithoutPartner)'}
    * def newTokenForUserWithoutPartner = newSessionForUserWithoutPartner.response.data.token

  @QPortalListPartnersV2
  Scenario: Retrieve Partners with pagination
    Given url utils.decodeUrl(baseUrl + '/qportal/v2/partners?page=1&per_page=20')
    And header Authorization = 'Bearer ' + bearer
    When method GET
    Then status 200

    * def response = $
    * print response
    * match response.data == '#present'
    * assert response.data.length > 1

  @QPortalListPartnersV2
  Scenario: Retrieve Partners with user without partners
    Given url utils.decodeUrl(baseUrl + '/qportal/v2/partners?page=1&per_page=20')
    And header Authorization = 'Bearer ' + newTokenForUserWithoutPartner
    When method GET
    Then status 200

    * def response = $
    * print response
    * match response.data == '#present'
    * assert response.data.length == 0