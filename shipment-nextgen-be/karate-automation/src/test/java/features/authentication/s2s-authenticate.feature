Feature: Authenticate

  Background:
    * url baseUrl
    * def s2sTokenDetails = read('classpath:session/json/s2s_token_and_organization.json')
    * def organizationId = s2sTokenDetails.organization_id
    * def s2sToken = s2sTokenDetails.s2s_token
    * def addMilestoneRequest = read('classpath:features/shipment/json/milestone/addMilestone.json')

  @S2SAuthentication @Regression
  Scenario: Access Milestone with valid S2S Token Details
    Given url utils.decodeUrl(baseUrl + '/milestones')
    And header X-API-AUTHORIZATION = s2sToken
    And header X-ORGANISATION-ID = organizationId
    And request addMilestoneRequest
    When method POST
    Then status 404

    * match response.data.order_no == '#notnull'
    * match response.data.segment_id == '#notnull'
    * match response.data.milestone == '#notnull'
    * match response.data.response_code == '#notnull'
    * match response.data.response_message == '#notnull'
    * match response.data.timestamp == '#notnull'

  @S2SAuthentication @Regression
  Scenario: Access Milestone with invalid S2S Token Details
    Given url utils.decodeUrl(baseUrl + '/milestones')
    And header X-API-AUTHORIZATION = 'Invalid token'
    And header X-ORGANISATION-ID = organizationId
    And request addMilestoneRequest
    When method POST
    Then status 401

  @S2SAuthentication @Regression
  Scenario: Access Milestone without S2S Token Details
    Given url utils.decodeUrl(baseUrl + '/milestones')
    And request addMilestoneRequest
    When method POST
    Then status 401