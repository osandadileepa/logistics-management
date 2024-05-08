Feature: Update Milestone Time

  Background:
    * url baseUrl
    * def bearer = token

    * def createShipmentResult = callonce read('classpath:features/shipment/getShipmentByShipmentTrackingIdWithS2SAuthentication.feature')
    * print createShipmentResult
    * def requestForUpdate = read('classpath:features/shipment/json/updateShipmentMilestoneAdditionalInfoRQ.json')
    * def shipment_tracking_id = createShipmentResult.response.data.shipment_tracking_id
    * print requestForUpdate

    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipment_tracking_id + '/milestone-and-additional-info')
    And header Authorization = 'Bearer '+ bearer
    And request requestForUpdate
    When method PATCH
    Then status 200

    * def response = $
    * print response
    * def requestForUpdate = read('classpath:features/shipment/json/milestone/updateMilestoneRQ.json')
    * def milestone_id = response.data.current_milestone_id

    * def updateRequest = read('classpath:features/shipment/json/milestone/updateMilestoneTimeRQ.json')
    * print updateRequest
    * print milestone_id

  @UpdateMilestone @Regression
  Scenario: Update Milestone Time
    Given url utils.decodeUrl(baseUrl + '/milestones/' + milestone_id + '/update-milestone-time')
    And header Authorization = 'Bearer '+ bearer
    And request requestForUpdate
    When method PATCH
    Then status 200

    * match response.data.milestone_time == '#notnull'

  @UpdateMilestone @Regression
  Scenario: Update Milestone Time with invalid milestone id
    * def milestone_id = 'INVALID_MILESTONE_ID'
    Given url utils.decodeUrl(baseUrl + '/milestones/' + milestone_id + '/update-milestone-time')
    And header Authorization = 'Bearer '+ bearer
    And request requestForUpdate
    When method PATCH
    Then status 404

  @UpdateMilestone @Permission
  Scenario: Update Milestone Time with milestone id but no location coverage access
    * def userCredentials = 'classpath:session/json/no_location_coverage.json'
    * def newSession = call read('classpath:session/create.feature') {userCredentials : '#(userCredentials)'}
    * def newToken = newSession.response.data.token
    * def bearer = newToken
    Given url utils.decodeUrl(baseUrl + '/milestones/' + milestone_id + '/update-milestone-time')
    And header Authorization = 'Bearer '+ bearer
    And request requestForUpdate
    When method PATCH
    Then status 403