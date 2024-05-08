Feature: Shipments > Permission: Partner Access

  Background:
    * url baseUrl
    * def bearer = token

    * def userCredentialsA = 'classpath:session/json/partner_user_001.json'
    * def newSessionA = callonce read('classpath:session/create.feature') {userCredentials : '#(userCredentialsA)'}
    * def newTokenA = newSessionA.response.data.token

    * def userCredentialsB = 'classpath:session/json/partner_user_002.json'
    * def newSessionB = callonce read('classpath:session/create.feature') {userCredentials : '#(userCredentialsB)'}
    * def newTokenB = newSessionB.response.data.token

    * def userCredentialsA1 = 'classpath:session/json/partner_user_extra_partner_001.json'
    * def newSessionA1 = callonce read('classpath:session/create.feature') {userCredentials : '#(userCredentialsA1)'}
    * def newTokenA1 = newSessionA1.response.data.token

    * def userCredentialsComp1 = 'classpath:session/json/company_user_extra_partner_001.json'
    * def newSessionComp1 = callonce read('classpath:session/create.feature') {userCredentials : '#(userCredentialsComp1)'}
    * def newTokenComp1 = newSessionComp1.response.data.token

    * def userCredentialsComp0 = 'classpath:session/json/company_user_no_partner.json'
    * def newSessionComp0 = callonce read('classpath:session/create.feature') {userCredentials : '#(userCredentialsComp0)'}
    * def newTokenComp0 = newSessionComp0.response.data.token

    * def prepareForUpdate =
    """
    function(request) {
      delete request.data.milestone
      delete request.data.milestone_events
      for (var i = 0; i < request.data.shipment_journey.package_journey_segments.length; i++) {
        delete request.data.shipment_journey.package_journey_segments[i].start_facility.id;
        delete request.data.shipment_journey.package_journey_segments[i].end_facility.id;
      }
      for (var i = 0; i < request.data.shipment_package.commodities.length; i++) {
        delete request.data.shipment_package.commodities[i].id
      }
      return request;
    }
    """

    * def jsonPathA = 'classpath:features/shipment/json/single/shipment-partner_001.json'
    * def createShipmentResultA = call read('classpath:features/shipment/addSingle.feature') {singleRQPath : '#(jsonPathA)'}
    * def shipmentIdA = createShipmentResultA.data.id
    * def shipmentTrackingIdA = createShipmentResultA.data.shipment_tracking_id
    * print shipmentIdA
    * def partnerIdA = createShipmentResultA.data.partner_id
    * print partnerIdA
    * def updateRequestA_ = prepareForUpdate(createShipmentResultA)
    * def updateRequestA = {}
    * updateRequestA.data = updateRequestA_.data
    * def updateJourneyRequestA = {}
    * updateJourneyRequestA.data = updateRequestA_.data.shipment_journey
    * updateJourneyRequestA.data.shipment_id = shipmentIdA
    * updateJourneyRequestA.data.order_id = updateRequestA_.data.order.id

    * def jsonPathB = 'classpath:features/shipment/json/single/shipment-partner_002.json'
    * def createShipmentResultB = call read('classpath:features/shipment/addSingle.feature') {singleRQPath : '#(jsonPathB)'}
    * def shipmentIdB = createShipmentResultB.data.id
    * def shipmentTrackingIdB = createShipmentResultB.data.shipment_tracking_id
    * print shipmentIdB
    * def partnerIdB = createShipmentResultB.data.partner_id
    * print partnerIdB
    * def updateRequestB_ = prepareForUpdate(createShipmentResultB)
    * def updateRequestB = {}
    * updateRequestB.data = updateRequestB_.data
    * def updateJourneyRequestB = {}
    * updateJourneyRequestB.data = updateRequestB_.data.shipment_journey
    * updateJourneyRequestB.data.shipment_id = shipmentIdB
    * updateJourneyRequestB.data.order_id = updateRequestB_.data.order.id

    * def jsonPath0 = 'classpath:features/shipment/json/single/shipment-no-partner.json'
    * def createShipmentResult0 = call read('classpath:features/shipment/addSingle.feature') {singleRQPath : '#(jsonPath0)'}
    * def shipmentId0 = createShipmentResult0.data.id
    * def shipmentTrackingId0 = createShipmentResult0.data.shipment_tracking_id
    * print shipmentId0
    * def partnerId0 = createShipmentResult0.data.partner_id
    * print partnerId0
    * def updateRequest0_ = prepareForUpdate(createShipmentResult0)
    * def updateRequest0 = {}
    * updateRequest0.data = updateRequest0_.data
    * def updateJourneyRequest0 = {}
    * updateJourneyRequest0.data = updateRequest0_.data.shipment_journey
    * updateJourneyRequest0.data.shipment_id = shipmentId0
    * updateJourneyRequest0.data.order_id = updateRequest0_.data.order.id

    * def findAllRequest = read('classpath:features/shipment/json/listRQ.json')
    * findAllRequest.data.size = 10

    * def exportRequest = read('classpath:features/shipment/json/exportRQ.json')
    * exportRequest.data.journey_status = null
    * exportRequest.data.keys = [ shipmentTrackingId0, shipmentTrackingIdA, shipmentTrackingIdB ]
    * print exportRequest

    * def requestForUpdateDimension = read('classpath:features/shipment/json/updatePackageDimensionRQ.json')
    * requestForUpdateDimension.data.organization_id = createShipmentResultA.data.customer.organization_id
    * delete requestForUpdateDimension.data.dimension_id
    * requestForUpdateDimension.data.length = 12.123
    * requestForUpdateDimension.data.width = 12.123
    * requestForUpdateDimension.data.height = 12.123
    * requestForUpdateDimension.data.gross_weight = 12.123
    * print requestForUpdateDimension

    * copy requestForUpdateDimensionA = requestForUpdateDimension
    * requestForUpdateDimensionA.data.shipment_tracking_id = shipmentTrackingIdA

    * copy requestForUpdateDimensionB = requestForUpdateDimension
    * requestForUpdateDimensionB.data.shipment_tracking_id = shipmentTrackingIdB

    * copy requestForUpdateDimension0 = requestForUpdateDimension
    * requestForUpdateDimension0.data.shipment_tracking_id = shipmentTrackingId0

    * def requestForUpdateMilestoneAdditionalInfo = read('classpath:features/shipment/json/updateShipmentMilestoneAdditionalInfoRQ.json')

    * copy requestForUpdateMilestoneAdditionalInfoA = requestForUpdateMilestoneAdditionalInfo
    * requestForUpdateMilestoneAdditionalInfoA.data.shipment_id = shipmentIdA

    * copy requestForUpdateMilestoneAdditionalInfoB = requestForUpdateMilestoneAdditionalInfo
    * requestForUpdateMilestoneAdditionalInfoB.data.shipment_id = shipmentIdB

    * copy requestForUpdateMilestoneAdditionalInfo0 = requestForUpdateMilestoneAdditionalInfo
    * requestForUpdateMilestoneAdditionalInfo0.data.shipment_id = shipmentId0

    * def dspMilestone = read('classpath:features/utility/json/milestoneMessage.json')

    * copy dspMilestoneA = dspMilestone
    * dspMilestoneA.partner_id = updateRequestA.data.partner_id
    * dspMilestoneA.shipment_id = shipmentIdA
    * dspMilestoneA.segment_id = createShipmentResultA.data.shipment_journey.package_journey_segments[0].segment_id

    * copy dspMilestoneB = dspMilestone
    * dspMilestoneB.partner_id = updateRequestB.data.partner_id
    * dspMilestoneB.shipment_id = shipmentIdB
    * dspMilestoneB.segment_id = createShipmentResultB.data.shipment_journey.package_journey_segments[0].segment_id

    * copy dspMilestone0 = dspMilestone
    * dspMilestone0.partner_id = updateRequest0.data.partner_id
    * dspMilestone0.shipment_id = shipmentId0
    * dspMilestone0.segment_id = createShipmentResult0.data.shipment_journey.package_journey_segments[0].segment_id

    * def requestForPartialUpdateMilestoneA = read('classpath:features/shipment/json/milestone/updateMilestoneRQ.json')
    * def requestForPartialUpdateMilestoneB = read('classpath:features/shipment/json/milestone/updateMilestoneRQ.json')
    * def requestForPartialUpdateMilestone0 = read('classpath:features/shipment/json/milestone/updateMilestoneRQ.json')

    * requestForPartialUpdateMilestoneA.data.shipment_id = shipmentIdA
    * requestForPartialUpdateMilestoneA.data.segment_id = '1'
    * requestForPartialUpdateMilestoneA.data.milestone_code = dspMilestoneA.milestone_code

    * requestForPartialUpdateMilestoneB.data.shipment_id = shipmentIdB
    * requestForPartialUpdateMilestoneB.data.segment_id = '1'
    * requestForPartialUpdateMilestoneB.data.milestone_code = dspMilestoneB.milestone_code

    * requestForPartialUpdateMilestone0.data.shipment_id = shipmentId0
    * requestForPartialUpdateMilestone0.data.segment_id = '1'
    * requestForPartialUpdateMilestone0.data.milestone_code = dspMilestone0.milestone_code

    * def shipmentWithAlertJsonA = 'classpath:features/shipment/json/single/shipment-partner_001-with-alerts.json'
    * def shipmentWithAlertJsonB = 'classpath:features/shipment/json/single/shipment-partner_002-with-alerts.json'
    * def shipmentWithAlertJson0 = 'classpath:features/shipment/json/single/shipment-no-partner-with-alerts.json'

    * def createdMilestoneA = call read('classpath:features/utility/simulateMilestoneFromDSP.feature') {jwtToken : '#(newTokenA)', requestPayload : '#(dspMilestoneA)'}
    * def createdMilestoneB = call read('classpath:features/utility/simulateMilestoneFromDSP.feature') {jwtToken : '#(newTokenB)', requestPayload : '#(dspMilestoneB)'}
    * def createdMilestone0 = call read('classpath:features/utility/simulateMilestoneFromDSP.feature') {jwtToken : '#(newTokenComp0)', requestPayload : '#(dspMilestone0)'}

    * def getShipmentIds = function(shp){ return shp.id }

  @Permissions
  Scenario: Happy path: Get shipment, user A (partner A) tries to access shipment created from partner A
    Given path '/shipments/' + shipmentIdA
    And header Authorization = 'Bearer ' + newTokenA
    When method GET
    Then status 200

    * def response = $
    * match response.data.id == shipmentIdA

  @Permissions
  Scenario: Unhappy path: Get shipment, user A (partner A) tries to access shipment created from partner B
    Given path '/shipments/' + shipmentIdB
    And header Authorization = 'Bearer '+ newTokenA
    When method GET
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Unhappy path: Get shipment, user A (partner A) tries to access shipment created from company (no partner)
    Given path '/shipments/' + shipmentId0
    And header Authorization = 'Bearer '+ newTokenA
    When method GET
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Happy path: Get shipment, user B (partner B) tries to access shipment created from partner B
    Given path '/shipments/' + shipmentIdB
    And header Authorization = 'Bearer ' + newTokenB
    When method GET
    Then status 200

  @Permissions
  Scenario: Unhappy path: Get shipment, user B (partner B) tries to access shipment created from partner A
    Given path '/shipments/' + shipmentIdA
    And header Authorization = 'Bearer '+ newTokenB
    When method GET
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Happy path: Get shipment, user A1 (extra partner A) tries to access shipment created from partner A
    Given path '/shipments/' + shipmentIdA
    And header Authorization = 'Bearer ' + newTokenA1
    When method GET
    Then status 200

    * def response = $
    * match response.data.id == shipmentIdA

  @Permissions
  Scenario: Unhappy path: Get shipment, user A (extra partner A) tries to access shipment created from partner B
    Given path '/shipments/' + shipmentIdB
    And header Authorization = 'Bearer '+ newTokenA1
    When method GET
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Unhappy path: Get shipment, user A (extra partner A) tries to access shipment created from company (no partner)
    Given path '/shipments/' + shipmentId0
    And header Authorization = 'Bearer '+ newTokenA1
    When method GET
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Unhappy path: Get shipment, company user tries to access shipment created from partner A
    Given path '/shipments/' + shipmentIdA
    And header Authorization = 'Bearer '+ newTokenComp0
    When method GET
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Happy path: Get shipment, company user tries to access shipment created from company (no partner)
    Given path '/shipments/' + shipmentId0
    And header Authorization = 'Bearer ' + newTokenComp0
    When method GET
    Then status 200

    * def response = $
    * match response.data.id == shipmentId0

  @Permissions
  Scenario: Happy path: Get shipment, company user (extra partner A) tries to access shipment created from partner A
    Given path '/shipments/' + shipmentIdA
    And header Authorization = 'Bearer ' + newTokenComp1
    When method GET
    Then status 200

    * def response = $
    * match response.data.id == shipmentIdA

  @Permissions
  Scenario: Unhappy path: Get shipment, company user (extra partner A) tries to access shipment created from partner B
    Given path '/shipments/' + shipmentIdB
    And header Authorization = 'Bearer '+ newTokenComp1
    When method GET
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Happy path: Get shipment, company user (extra partner A) tries to access shipment created from company (no partner)
    Given path '/shipments/' + shipmentId0
    And header Authorization = 'Bearer ' + newTokenComp1
    When method GET
    Then status 200

    * def response = $
    * match response.data.id == shipmentId0

  @Permissions
  Scenario: User A get shipment listing page
    Given path '/shipments/list'
    And header Authorization = 'Bearer '+ newTokenA
    And request findAllRequest
    When method POST
    Then status 200

    * def response = $
    * def data = response.data
    * assert data.total_elements >= 1
    * def shipmentIds = karate.map(data.result, getShipmentIds)
    * print shipmentIds
    * match shipmentIds contains shipmentIdA
    * match shipmentIds !contains shipmentIdB
    * match shipmentIds !contains shipmentId0

  @Permissions
  Scenario: User B get shipment listing page
    Given path '/shipments/list'
    And header Authorization = 'Bearer '+ newTokenB
    And request findAllRequest
    When method POST
    Then status 200

    * def response = $
    * def data = response.data
    * assert data.total_elements >= 1
    * def shipmentIds = karate.map(data.result, getShipmentIds)
    * print shipmentIds
    * match shipmentIds contains shipmentIdB
    * match shipmentIds !contains shipmentIdA
    * match shipmentIds !contains shipmentId0

  @Permissions
  Scenario: User A1 (extra partner A) get shipment listing page
    Given path '/shipments/list'
    And header Authorization = 'Bearer '+ newTokenA1
    And request findAllRequest
    When method POST
    Then status 200

    * def response = $
    * def data = response.data
    * assert data.total_elements >= 1
    * def shipmentIds = karate.map(data.result, getShipmentIds)
    * print shipmentIds
    * match shipmentIds contains shipmentIdA
    * match shipmentIds !contains shipmentIdB
    * match shipmentIds !contains shipmentId0

  @Permissions
  Scenario: Company User get shipment listing page
    Given path '/shipments/list'
    And header Authorization = 'Bearer '+ newTokenComp0
    And request findAllRequest
    When method POST
    Then status 200

    * def response = $
    * def data = response.data
    * assert data.total_elements >= 1
    * def shipmentIds = karate.map(data.result, getShipmentIds)
    * print shipmentIds
    * match shipmentIds contains shipmentId0
    * match shipmentIds !contains shipmentIdA
    * match shipmentIds !contains shipmentIdB

  @Permissions
  Scenario: Company User (extra partner A) get shipment listing page
    Given path '/shipments/list'
    And header Authorization = 'Bearer '+ newTokenComp1
    And request findAllRequest
    When method POST
    Then status 200

    * def response = $
    * def data = response.data
    * assert data.total_elements >= 1
    * def shipmentIds = karate.map(data.result, getShipmentIds)
    * print shipmentIds
    * match shipmentIds contains shipmentId0
    * match shipmentIds contains shipmentIdA
    * match shipmentIds !contains shipmentIdB

  @Permissions
  Scenario: User A exports shipments
    Given path '/shipments/export'
    And header Authorization = 'Bearer '+ newTokenA
    And request exportRequest
    When method POST
    Then status 200

    * def response = $
    * print response
    * def shipmentIdsExported = utils.getShipmentIdsFromCsv(response)
    * print shipmentIdsExported
    * match shipmentIdsExported contains shipmentTrackingIdA
    * match shipmentIdsExported !contains shipmentTrackingIdB
    * match shipmentIdsExported !contains shipmentTrackingId0

  @Permissions
  Scenario: User B exports shipments
    Given path '/shipments/export'
    And header Authorization = 'Bearer '+ newTokenB
    And request exportRequest
    When method POST
    Then status 200

    * def response = $
    * print response
    * def shipmentIdsExported = utils.getShipmentIdsFromCsv(response)
    * print shipmentIdsExported
    * match shipmentIdsExported !contains shipmentTrackingIdA
    * match shipmentIdsExported contains shipmentTrackingIdB
    * match shipmentIdsExported !contains shipmentTrackingId0

  @Permissions
  Scenario: User A1 (extra partner A) exports shipments
    Given path '/shipments/export'
    And header Authorization = 'Bearer '+ newTokenA1
    And request exportRequest
    When method POST
    Then status 200

    * def response = $
    * print response
    * def shipmentIdsExported = utils.getShipmentIdsFromCsv(response)
    * print shipmentIdsExported
    * match shipmentIdsExported contains shipmentTrackingIdA
    * match shipmentIdsExported !contains shipmentTrackingIdB
    * match shipmentIdsExported !contains shipmentTrackingId0

  @Permissions
  Scenario: Company User exports shipments
    Given path '/shipments/export'
    And header Authorization = 'Bearer '+ newTokenComp0
    And request exportRequest
    When method POST
    Then status 200

    * def response = $
    * print response
    * def shipmentIdsExported = utils.getShipmentIdsFromCsv(response)
    * print shipmentIdsExported
    * match shipmentIdsExported !contains shipmentTrackingIdA
    * match shipmentIdsExported !contains shipmentTrackingIdB
    * match shipmentIdsExported contains shipmentTrackingId0

  @Permissions
  Scenario: Company User (extra partner A) exports shipments
    Given path '/shipments/export'
    And header Authorization = 'Bearer '+ newTokenComp1
    And request exportRequest
    When method POST
    Then status 200

    * def response = $
    * print response
    * def shipmentIdsExported = utils.getShipmentIdsFromCsv(response)
    * print shipmentIdsExported
    * match shipmentIdsExported contains shipmentTrackingIdA
    * match shipmentIdsExported !contains shipmentTrackingIdB
    * match shipmentIdsExported contains shipmentTrackingId0

  @Permissions
  Scenario: Happy path: Update shipment, user A (partner A) tries to update shipment created from partner A
    Given path '/shipments'
    And header Authorization = 'Bearer ' + newTokenA
    And request updateRequestA
    When method PUT
    Then status 200

    * def response = $
    * match response.data.id == shipmentIdA

  @Permissions
  Scenario: Unhappy path: Update shipment, user A (partner A) tries to update shipment created from partner B
    Given path '/shipments'
    And header Authorization = 'Bearer '+ newTokenA
    And request updateRequestB
    When method PUT
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Unhappy path: Update shipment, user A (partner A) tries to update shipment created from company (no partner)
    Given path '/shipments'
    And header Authorization = 'Bearer '+ newTokenA
    And request updateRequest0
    When method PUT
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Happy path: Update shipment, user B (partner B) tries to update shipment created from partner B
    Given path '/shipments'
    And header Authorization = 'Bearer ' + newTokenB
    And request updateRequestB
    When method PUT
    Then status 200

    * def response = $
    * match response.data.id == shipmentIdB

  @Permissions
  Scenario: Unhappy path: Update shipment, user B (partner B) tries to update shipment created from partner A
    Given path '/shipments'
    And header Authorization = 'Bearer '+ newTokenB
    And request updateRequestA
    When method PUT
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Happy path: Update shipment, user A1 (extra partner A) tries to update shipment created from partner A
    Given path '/shipments'
    And header Authorization = 'Bearer ' + newTokenA1
    And request updateRequestA
    When method PUT
    Then status 200

    * def response = $
    * match response.data.id == shipmentIdA

  @Permissions
  Scenario: Unhappy path: Update shipment, user A1 (extra partner A) tries to update shipment created from partner B
    Given path '/shipments'
    And header Authorization = 'Bearer '+ newTokenA1
    And request updateRequestB
    When method PUT
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Unhappy path: Update shipment, user A1 (extra partner A) tries to update shipment created from company (no partner)
    Given path '/shipments'
    And header Authorization = 'Bearer '+ newTokenA1
    And request updateRequest0
    When method PUT
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Happy path: Update shipment, company user tries to update shipment created from company
    Given path '/shipments'
    And header Authorization = 'Bearer ' + newTokenComp0
    And request updateRequest0
    When method PUT
    Then status 200

    * def response = $
    * match response.data.id == shipmentId0

  @Permissions
  Scenario: Unhappy path: Update shipment, company user tries to update shipment created from partner A
    Given path '/shipments'
    And header Authorization = 'Bearer '+ newTokenComp0
    And request updateRequestA
    When method PUT
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Happy path: Update shipment, company user (extra partner A) tries to update shipment created from company
    Given path '/shipments'
    And header Authorization = 'Bearer ' + newTokenComp1
    And request updateRequest0
    When method PUT
    Then status 200

    * def response = $
    * match response.data.id == shipmentId0

  @Permissions
  Scenario: Happy path: Update shipment, company user (extra partner A) tries to update shipment created from partner A
    Given path '/shipments'
    And header Authorization = 'Bearer '+ newTokenComp1
    And request updateRequestA
    When method PUT
    Then status 200

    * def response = $
    * match response.data.id == shipmentIdA

  @Permissions
  Scenario: Unhappy path: Update shipment, company user (extra partner A) tries to update shipment created from partner B
    Given path '/shipments'
    And header Authorization = 'Bearer '+ newTokenComp1
    And request updateRequestB
    When method PUT
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Happy path: Update shipment, company user (extra partner A) tries to update shipment created from company (no partner)
    Given path '/shipments'
    And header Authorization = 'Bearer '+ newTokenComp1
    And request updateRequest0
    When method PUT
    Then status 200

    * def response = $
    * match response.data.id == shipmentId0

  @Permissions
  Scenario: Happy path: Get journey, user A (partner A) tries to access journey created from partner A
    Given path '/shipments/' + shipmentIdA + '/shipment_journey'
    And header Authorization = 'Bearer ' + newTokenA
    When method GET
    Then status 200

    * def response = $
    * match response.data.journey_id == '#present'

  @Permissions
  Scenario: Unhappy path: Get journey, user A (partner A) tries to access journey created from partner B
    Given path '/shipments/' + shipmentIdB + '/shipment_journey'
    And header Authorization = 'Bearer '+ newTokenA
    When method GET
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Unhappy path: Get journey, user A (partner A) tries to access journey created from company (no partner)
    Given path '/shipments/' + shipmentId0 + '/shipment_journey'
    And header Authorization = 'Bearer '+ newTokenA
    When method GET
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Happy path: Get journey, user B (partner B) tries to access journey created from partner B
    Given path '/shipments/' + shipmentIdB + '/shipment_journey'
    And header Authorization = 'Bearer ' + newTokenB
    When method GET
    Then status 200

    * def response = $
    * match response.data.journey_id == '#present'

  @Permissions
  Scenario: Unhappy path: Get journey, user B (partner B) tries to access journey created from partner A
    Given path '/shipments/' + shipmentIdA + '/shipment_journey'
    And header Authorization = 'Bearer '+ newTokenB
    When method GET
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Happy path: Get journey, user A1 (extra partner A) tries to access journey created from partner A
    Given path '/shipments/' + shipmentIdA + '/shipment_journey'
    And header Authorization = 'Bearer ' + newTokenA1
    When method GET
    Then status 200

    * def response = $
    * match response.data.journey_id == '#present'

  @Permissions
  Scenario: Unhappy path: Get journey, user A1 (extra partner A) tries to access journey created from partner B
    Given path '/shipments/' + shipmentIdB + '/shipment_journey'
    And header Authorization = 'Bearer '+ newTokenA1
    When method GET
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Unhappy path: Get journey, user A1 (extra partner A) tries to access journey created from company (no partner)
    Given path '/shipments/' + shipmentId0 + '/shipment_journey'
    And header Authorization = 'Bearer ' + newTokenA1
    When method GET
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Unhappy path: Get journey, company user tries to access journey created from partner A
    Given path '/shipments/' + shipmentIdA + '/shipment_journey'
    And header Authorization = 'Bearer ' + newTokenComp0
    When method GET
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Happy path: Get journey, company user tries to access journey created from company (no partner)
    Given path '/shipments/' + shipmentId0 + '/shipment_journey'
    And header Authorization = 'Bearer ' + newTokenComp0
    When method GET
    Then status 200

    * def response = $
    * match response.data.journey_id == '#present'

  @Permissions
  Scenario: Happy path: Get journey, company user (extra partner A) tries to access journey created from partner A
    Given path '/shipments/' + shipmentIdA + '/shipment_journey'
    And header Authorization = 'Bearer '+ newTokenComp1
    When method GET
    Then status 200

    * def response = $
    * match response.data.journey_id == '#present'

  @Permissions
  Scenario: Unhappy path: Get journey, company user (extra partner A) tries to access journey created from partner B
    Given path '/shipments/' + shipmentIdB + '/shipment_journey'
    And header Authorization = 'Bearer ' + newTokenComp1
    When method GET
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Happy path: Update journey, user A (partner A) tries to update journey created from partner A
    Given path '/shipments/shipment_journey'
    And header Authorization = 'Bearer ' + newTokenA
    And request updateJourneyRequestA
    When method PUT
    Then status 200

    * def response = $
    * match response.data.shipment_journey == '#present'
    * match response.data.updated_shipment_tracking_ids contains shipmentTrackingIdA
    * match response.data.total_shipments_updated == 1

  @Permissions
  Scenario: Unhappy path: Update journey, user A (partner A) tries to update journey created from partner B
    Given path '/shipments/shipment_journey'
    And header Authorization = 'Bearer ' + newTokenA
    And request updateJourneyRequestB
    When method PUT
    Then status 403

    * def response = $
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Unhappy path: Update journey, user A (partner A) tries to update journey created from company (no partner)
    Given path '/shipments/shipment_journey'
    And header Authorization = 'Bearer ' + newTokenA
    And request updateJourneyRequest0
    When method PUT
    Then status 403

    * def response = $
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Happy path: Update journey, user B (partner B) tries to update journey created from partner B
    Given path '/shipments/shipment_journey'
    And header Authorization = 'Bearer ' + newTokenB
    And request updateJourneyRequestB
    When method PUT
    Then status 200

    * def response = $
    * match response.data.shipment_journey == '#present'
    * match response.data.updated_shipment_tracking_ids contains shipmentTrackingIdB
    * match response.data.total_shipments_updated == 1

  @Permissions
  Scenario: Unhappy path: Update journey, user B (partner B) tries to update journey created from partner A
    Given path '/shipments/shipment_journey'
    And header Authorization = 'Bearer ' + newTokenB
    And request updateJourneyRequestA
    When method PUT
    Then status 403

    * def response = $
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Happy path: Update journey, user A1 (extra partner A) tries to update journey created from partner A
    Given path '/shipments/shipment_journey'
    And header Authorization = 'Bearer ' + newTokenA1
    And request updateJourneyRequestA
    When method PUT
    Then status 200

    * def response = $
    * match response.data.shipment_journey == '#present'
    * match response.data.updated_shipment_tracking_ids contains shipmentTrackingIdA
    * match response.data.total_shipments_updated == 1

  @Permissions
  Scenario: Unhappy path: Update journey, user A1 (extra partner A) tries to update journey created from partner B
    Given path '/shipments/shipment_journey'
    And header Authorization = 'Bearer ' + newTokenA1
    And request updateJourneyRequestB
    When method PUT
    Then status 403

    * def response = $
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Unhappy path: Update journey, user A1 (extra partner A) tries to update journey created from company (no partner)
    Given path '/shipments/shipment_journey'
    And header Authorization = 'Bearer ' + newTokenA1
    And request updateJourneyRequest0
    When method PUT
    Then status 403

    * def response = $
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Unhappy path: Update journey, company user tries to update journey created from partner A
    Given path '/shipments/shipment_journey'
    And header Authorization = 'Bearer ' + newTokenComp0
    And request updateJourneyRequestA
    When method PUT
    Then status 403

    * def response = $
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Happy path: Update journey, company user tries to update journey created from company (no partner)
    Given path '/shipments/shipment_journey'
    And header Authorization = 'Bearer ' + newTokenComp0
    And request updateJourneyRequest0
    When method PUT
    Then status 200

    * def response = $
    * match response.data.shipment_journey == '#present'
    * match response.data.updated_shipment_tracking_ids contains shipmentTrackingId0
    * match response.data.total_shipments_updated == 1

  @Permissions
  Scenario: Happy path: Update journey, company user (extra partner A) tries to update journey created from partner A
    Given path '/shipments/shipment_journey'
    And header Authorization = 'Bearer ' + newTokenComp1
    And request updateJourneyRequestA
    When method PUT
    Then status 200

    * def response = $
    * match response.data.shipment_journey == '#present'
    * match response.data.updated_shipment_tracking_ids contains shipmentTrackingIdA
    * match response.data.total_shipments_updated == 1

  @Permissions
  Scenario: Unhappy path: Update journey, company user (extra partner A) tries to update journey created from partner B
    Given path '/shipments/shipment_journey'
    And header Authorization = 'Bearer ' + newTokenComp1
    And request updateJourneyRequestB
    When method PUT
    Then status 403

    * def response = $
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Happy path: Update journey, company user (extra partner A) tries to update journey created from company (no partner)
    Given path '/shipments/shipment_journey'
    And header Authorization = 'Bearer ' + newTokenComp1
    And request updateJourneyRequest0
    When method PUT
    Then status 200

    * def response = $
    * match response.data.shipment_journey == '#present'
    * match response.data.updated_shipment_tracking_ids contains shipmentTrackingId0
    * match response.data.total_shipments_updated == 1

  @Permissions
  Scenario: Happy path: Get journey, company user (extra partner A) tries to access journey created from company (no partner)
    Given path '/shipments/' + shipmentId0 + '/shipment_journey'
    And header Authorization = 'Bearer '+ newTokenComp1
    When method GET
    Then status 200

    * def response = $
    * match response.data.journey_id == '#present'

  @Permissions
  Scenario: Happy path: Get package dimension, user A (partner A) tries to access dimension created from partner A
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipmentTrackingIdA + '/package-dimension')
    And header Authorization = 'Bearer ' + newTokenA
    When method GET
    Then status 200

    * def response = $
    * match response.data.id == shipmentIdA

  @Permissions
  Scenario: Unhappy path: Get package dimension, user A (partner A) tries to access dimension created from partner B
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipmentTrackingIdB + '/package-dimension')
    And header Authorization = 'Bearer '+ newTokenA
    When method GET
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Unhappy path: Get package dimension, user A (partner A) tries to access dimension created from company (no partner)
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipmentTrackingId0 + '/package-dimension')
    And header Authorization = 'Bearer '+ newTokenA
    When method GET
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Happy path: Get package dimension, user B (partner B) tries to access dimension created from partner B
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipmentTrackingIdB + '/package-dimension')
    And header Authorization = 'Bearer ' + newTokenB
    When method GET
    Then status 200

    * def response = $
    * match response.data.id == shipmentIdB

  @Permissions
  Scenario: Unhappy path: Get package dimension, user B (partner B) tries to access dimension created from company (no partner)
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipmentTrackingId0 + '/package-dimension')
    And header Authorization = 'Bearer '+ newTokenB
    When method GET
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Happy path: Get package dimension, user A1 (extra partner A) tries to access dimension created from partner A
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipmentTrackingIdA + '/package-dimension')
    And header Authorization = 'Bearer ' + newTokenA1
    When method GET
    Then status 200

    * def response = $
    * match response.data.id == shipmentIdA

  @Permissions
  Scenario: Unhappy path: Get package dimension, user A1 (extra partner A) tries to access dimension created from partner B
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipmentTrackingIdB + '/package-dimension')
    And header Authorization = 'Bearer '+ newTokenA1
    When method GET
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Unhappy path: Get package dimension, user A1 (extra partner A) tries to access dimension created from company (no partner)
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipmentTrackingId0 + '/package-dimension')
    And header Authorization = 'Bearer '+ newTokenA1
    When method GET
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Unhappy path: Get package dimension, company user tries to access dimension created from partner A
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipmentTrackingIdA + '/package-dimension')
    And header Authorization = 'Bearer '+ newTokenComp0
    When method GET
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Happy path: Get package dimension, company user tries to access dimension created from company user (no partner)
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipmentTrackingId0 + '/package-dimension')
    And header Authorization = 'Bearer ' + newTokenComp0
    When method GET
    Then status 200

    * def response = $
    * match response.data.id == shipmentId0

  @Permissions
  Scenario: Happy path: Get package dimension, company user (extra partner A) tries to access dimension created from partner A
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipmentTrackingIdA + '/package-dimension')
    And header Authorization = 'Bearer ' + newTokenComp1
    When method GET
    Then status 200

    * def response = $
    * match response.data.id == shipmentIdA

  @Permissions
  Scenario: Unhappy path: Get package dimension, company user (extra partner A) tries to access dimension created from partner B
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipmentTrackingIdB + '/package-dimension')
    And header Authorization = 'Bearer '+ newTokenComp1
    When method GET
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Happy path: Get package dimension, company user (extra partner A) tries to access dimension created from company user (no partner)
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipmentTrackingId0 + '/package-dimension')
    And header Authorization = 'Bearer ' + newTokenComp1
    When method GET
    Then status 200

    * def response = $
    * match response.data.id == shipmentId0

  @Permissions
  Scenario: Happy path: Update package dimension, user A (partner A) tries to update dimension created from partner A
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipmentTrackingIdA + '/package-dimension')
    And header Authorization = 'Bearer ' + newTokenA
    And request requestForUpdateDimensionA
    When method PUT
    Then status 200

    * def response = $
    * match response.data.shipment_package.total_items_count == 1

  @Permissions
  Scenario: Unhappy path: Update package dimension, user A (partner A) tries to update dimension created from partner B
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipmentTrackingIdB + '/package-dimension')
    And header Authorization = 'Bearer ' + newTokenA
    And request requestForUpdateDimensionB
    When method PUT
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Unhappy path: Update package dimension, user A (partner A) tries to update dimension created from company (no partner)
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipmentTrackingId0 + '/package-dimension')
    And header Authorization = 'Bearer ' + newTokenA
    And request requestForUpdateDimension0
    When method PUT
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Happy path: Update package dimension, user B (partner B) tries to update dimension created from partner B
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipmentTrackingIdB + '/package-dimension')
    And header Authorization = 'Bearer ' + newTokenB
    And request requestForUpdateDimensionB
    When method PUT
    Then status 200

    * def response = $
    * match response.data.shipment_package.total_items_count == 1

  @Permissions
  Scenario: Unhappy path: Update package dimension, user B (partner B) tries to update dimension created from partner A
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipmentTrackingIdA + '/package-dimension')
    And header Authorization = 'Bearer ' + newTokenB
    And request requestForUpdateDimensionA
    When method PUT
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Happy path: Update package dimension, user A1 (extra partner A) tries to update dimension created from partner A
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipmentTrackingIdA + '/package-dimension')
    And header Authorization = 'Bearer ' + newTokenA1
    And request requestForUpdateDimensionA
    When method PUT
    Then status 200

    * def response = $
    * match response.data.shipment_package.total_items_count == 1

  @Permissions
  Scenario: Unhappy path: Update package dimension, user A1 (extra partner A) tries to update dimension created from partner B
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipmentTrackingIdB + '/package-dimension')
    And header Authorization = 'Bearer ' + newTokenA1
    And request requestForUpdateDimensionB
    When method PUT
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Unhappy path: Update package dimension, user A1 (extra partner A) tries to update dimension created from company (no partner)
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipmentTrackingId0 + '/package-dimension')
    And header Authorization = 'Bearer ' + newTokenA1
    And request requestForUpdateDimension0
    When method PUT
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Unhappy path: Update package dimension, company user tries to update dimension created from partner A
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipmentTrackingIdA + '/package-dimension')
    And header Authorization = 'Bearer ' + newTokenComp0
    And request requestForUpdateDimensionA
    When method PUT
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Happy path: Update package dimension, company user tries to update dimension created from company (no partner)
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipmentTrackingId0 + '/package-dimension')
    And header Authorization = 'Bearer ' + newTokenComp0
    And request requestForUpdateDimension0
    When method PUT
    Then status 200

    * def response = $
    * match response.data.shipment_package.total_items_count == 1

  @Permissions
  Scenario: Happy path: Update package dimension, company user (extra partner A) tries to update dimension created from partner A
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipmentTrackingIdA + '/package-dimension')
    And header Authorization = 'Bearer ' + newTokenComp1
    And request requestForUpdateDimensionA
    When method PUT
    Then status 200

    * def response = $
    * match response.data.shipment_package.total_items_count == 1

  @Permissions
  Scenario: Unhappy path: Update package dimension, company user (extra partner A) tries to update dimension created from partner B
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipmentTrackingIdB + '/package-dimension')
    And header Authorization = 'Bearer ' + newTokenComp1
    And request requestForUpdateDimensionB
    When method PUT
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Happy path: Update package dimension, company user (extra partner A) tries to update dimension created company (no user)
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipmentTrackingId0 + '/package-dimension')
    And header Authorization = 'Bearer ' + newTokenComp1
    And request requestForUpdateDimension0
    When method PUT
    Then status 200

    * def response = $
    * match response.data.shipment_package.total_items_count == 1

  @Permissions
  Scenario: Happy path: Add Milestone w/ Additional Info, user A (partner A) tries to modify milestone related to shipment from partner A
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipmentTrackingIdA + '/milestone-and-additional-info')
    And header Authorization = 'Bearer ' + newTokenA
    And request requestForUpdateMilestoneAdditionalInfoA
    When method PATCH
    Then status 200

    * def response = $
    * match response.data.shipment_id == shipmentIdA

  @Permissions
  Scenario: Unhappy path: Add Milestone w/ Additional Info, user A (partner A) tries to modify milestone related to shipment from partner B
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipmentTrackingIdB + '/milestone-and-additional-info')
    And header Authorization = 'Bearer '+ newTokenA
    And request requestForUpdateMilestoneAdditionalInfoB
    When method PATCH
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Unhappy path: Add Milestone w/ Additional Info, user A (partner A) tries to modify milestone related to shipment from company (no partner)
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipmentTrackingId0 + '/milestone-and-additional-info')
    And header Authorization = 'Bearer '+ newTokenA
    And request requestForUpdateMilestoneAdditionalInfo0
    When method PATCH
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Happy path: Add Milestone w/ Additional Info, user B (partner B) tries to modify milestone related to shipment from partner B
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipmentTrackingIdB + '/milestone-and-additional-info')
    And header Authorization = 'Bearer ' + newTokenB
    And request requestForUpdateMilestoneAdditionalInfoA
    When method PATCH
    Then status 200

    * def response = $
    * match response.data.shipment_id == shipmentIdB

  @Permissions
  Scenario: Unhappy path: Add Milestone w/ Additional Info, user B (partner B) tries to modify milestone related to shipment from partner A
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipmentTrackingIdA + '/milestone-and-additional-info')
    And header Authorization = 'Bearer '+ newTokenB
    And request requestForUpdateMilestoneAdditionalInfoA
    When method PATCH
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Happy path: Add Milestone w/ Additional Info, user A1 (extra partner A) tries to modify milestone related to shipment from partner A
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipmentTrackingIdA + '/milestone-and-additional-info')
    And header Authorization = 'Bearer ' + newTokenA1
    And request requestForUpdateMilestoneAdditionalInfoA
    When method PATCH
    Then status 200

    * def response = $
    * match response.data.shipment_id == shipmentIdA

  @Permissions
  Scenario: Unhappy path: Add Milestone w/ Additional Info, user A1 (extra partner A) tries to modify milestone related to shipment from partner B
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipmentTrackingIdB + '/milestone-and-additional-info')
    And header Authorization = 'Bearer '+ newTokenA1
    And request requestForUpdateMilestoneAdditionalInfoB
    When method PATCH
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Unhappy path: Add Milestone w/ Additional Info, user A1 (extra partner A) tries to modify milestone related to shipment from company (no partner)
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipmentTrackingId0 + '/milestone-and-additional-info')
    And header Authorization = 'Bearer '+ newTokenA1
    And request requestForUpdateMilestoneAdditionalInfo0
    When method PATCH
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Unhappy path: Add Milestone w/ Additional Info, company user tries to modify milestone related to shipment from partner A
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipmentTrackingIdA + '/milestone-and-additional-info')
    And header Authorization = 'Bearer '+ newTokenComp0
    And request requestForUpdateMilestoneAdditionalInfoA
    When method PATCH
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Happy path: Add Milestone w/ Additional Info, company user tries to modify milestone related to shipment from company (no partner)
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipmentTrackingId0 + '/milestone-and-additional-info')
    And header Authorization = 'Bearer ' + newTokenComp0
    And request requestForUpdateMilestoneAdditionalInfo0
    When method PATCH
    Then status 200

    * def response = $
    * match response.data.shipment_id == shipmentId0

  @Permissions
  Scenario: Happy path: Add Milestone w/ Additional Info, company user (extra partner A) tries to modify milestone related to shipment from partner A
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipmentTrackingIdA + '/milestone-and-additional-info')
    And header Authorization = 'Bearer ' + newTokenComp1
    And request requestForUpdateMilestoneAdditionalInfoA
    When method PATCH
    Then status 200

    * def response = $
    * match response.data.shipment_id == shipmentIdA

  @Permissions
  Scenario: Unhappy path: Add Milestone w/ Additional Info, company user (extra partner A) tries to modify milestone related to shipment from partner B
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipmentTrackingIdB + '/milestone-and-additional-info')
    And header Authorization = 'Bearer '+ newTokenComp1
    And request requestForUpdateMilestoneAdditionalInfoB
    When method PATCH
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Happy path: Add Milestone w/ Additional Info, company user (extra partner A) tries to modify milestone related to shipment from company (no partner)
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipmentTrackingId0 + '/milestone-and-additional-info')
    And header Authorization = 'Bearer ' + newTokenComp1
    And request requestForUpdateMilestoneAdditionalInfo0
    When method PATCH
    Then status 200

    * def response = $
    * match response.data.shipment_id == shipmentId0

  @Permissions
  Scenario: Happy path: Partial Update Milestone, user A (partner A) tries to updates milestone for shipment created from partner A
    Given path '/milestones/partial-update'
    And header Authorization = 'Bearer ' + newTokenA
    And request requestForPartialUpdateMilestoneA
    When method PATCH
    Then status 200

    * def response = $
    * match response.data.shipment_id == shipmentIdA

  @Permissions
  Scenario: Unhappy path: Partial Update Milestone, user A (partner A) tries to updates milestone for shipment created from partner B
    Given path '/milestones/partial-update'
    And header Authorization = 'Bearer ' + newTokenA
    And request requestForPartialUpdateMilestoneB
    When method PATCH
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Unhappy path: Partial Update Milestone, user A (partner A) tries to updates milestone for shipment created from company (no partner)
    Given path '/milestones/partial-update'
    And header Authorization = 'Bearer ' + newTokenA
    And request requestForPartialUpdateMilestone0
    When method PATCH
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Happy path: Partial Update Milestone, user B (partner B) tries to updates milestone for shipment created from partner B
    Given path '/milestones/partial-update'
    And header Authorization = 'Bearer ' + newTokenB
    And request requestForPartialUpdateMilestoneB
    When method PATCH
    Then status 200

    * def response = $
    * match response.data.shipment_id == shipmentIdB

  @Permissions
  Scenario: Unhappy path: Partial Update Milestone, user B (partner B) tries to updates milestone for shipment created from partner A
    Given path '/milestones/partial-update'
    And header Authorization = 'Bearer ' + newTokenB
    And request requestForPartialUpdateMilestoneA
    When method PATCH
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Happy path: Partial Update Milestone, user A1 (extra partner A) tries to updates milestone for shipment created from partner A
    Given path '/milestones/partial-update'
    And header Authorization = 'Bearer ' + newTokenA1
    And request requestForPartialUpdateMilestoneA
    When method PATCH
    Then status 200

    * def response = $
    * match response.data.shipment_id == shipmentIdA

  @Permissions
  Scenario: Unhappy path: Partial Update Milestone, user A1 (extra partner A) tries to updates milestone for shipment created from partner B
    Given path '/milestones/partial-update'
    And header Authorization = 'Bearer ' + newTokenA1
    And request requestForPartialUpdateMilestoneB
    When method PATCH
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Unhappy path: Partial Update Milestone, user A1 (extra partner A) tries to updates milestone for shipment created from company (no partner)
    Given path '/milestones/partial-update'
    And header Authorization = 'Bearer ' + newTokenA1
    And request requestForPartialUpdateMilestone0
    When method PATCH
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Unhappy path: Partial Update Milestone, company user tries to updates milestone for shipment created from partner A
    Given path '/milestones/partial-update'
    And header Authorization = 'Bearer ' + newTokenComp0
    And request requestForPartialUpdateMilestoneA
    When method PATCH
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Happy path: Partial Update Milestone, company user tries to updates milestone for shipment created from company (no partner)
    Given path '/milestones/partial-update'
    And header Authorization = 'Bearer ' + newTokenComp0
    And request requestForPartialUpdateMilestone0
    When method PATCH
    Then status 200

    * def response = $
    * match response.data.shipment_id == shipmentId0

  @Permissions
  Scenario: Happy path: Partial Update Milestone, company user (extra partner A) tries to updates milestone for shipment created from partner A
    Given path '/milestones/partial-update'
    And header Authorization = 'Bearer ' + newTokenComp1
    And request requestForPartialUpdateMilestoneA
    When method PATCH
    Then status 200

    * def response = $
    * match response.data.shipment_id == shipmentIdA

  @Permissions
  Scenario: Unhappy path: Partial Update Milestone, company user (extra partner A) tries to updates milestone for shipment created from partner B
    Given path '/milestones/partial-update'
    And header Authorization = 'Bearer ' + newTokenComp1
    And request requestForPartialUpdateMilestoneB
    When method PATCH
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Happy path: Partial Update Milestone, company user (extra partner A) tries to updates milestone for shipment created from company (no partner)
    Given path '/milestones/partial-update'
    And header Authorization = 'Bearer ' + newTokenComp1
    And request requestForPartialUpdateMilestone0
    When method PATCH
    Then status 200

    * def response = $
    * match response.data.shipment_id == shipmentId0

  @Permissions
  Scenario: Happy path: Dismiss shipment alert, user A (partner A) tries to dismiss shipment alert created from partner A
    * def createShipmentWithAlertResultA = call read('classpath:features/shipment/addSingleWithAlerts.feature') {singleRQPath : '#(shipmentWithAlertJsonA)'}
    * def segmentAlertIdToUpdateA =  createShipmentWithAlertResultA.response.data.shipment_journey.package_journey_segments[0].alerts[0].id

    Given path '/alerts/' + segmentAlertIdToUpdateA
    Given param dismissed = true
    And header Authorization = 'Bearer ' + newTokenA
    When method PATCH
    Then status 204

  @Permissions
  Scenario: Unhappy path: Dismiss shipment alert, user A (partner A) tries to dismiss shipment alert created from partner B
    * def createShipmentWithAlertResultB = call read('classpath:features/shipment/addSingleWithAlerts.feature') {singleRQPath : '#(shipmentWithAlertJsonB)'}
    * def segmentAlertIdToUpdateB =  createShipmentWithAlertResultB.response.data.shipment_journey.package_journey_segments[0].alerts[0].id

    Given path '/alerts/' + segmentAlertIdToUpdateB
    Given param dismissed = true
    And header Authorization = 'Bearer ' + newTokenA
    When method PATCH
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Unhappy path: Dismiss shipment alert, user A (partner A) tries to dismiss shipment alert created from company (no partner)
    * def createShipmentWithAlertResult0 = call read('classpath:features/shipment/addSingleWithAlerts.feature') {singleRQPath : '#(shipmentWithAlertJson0)'}
    * def segmentAlertIdToUpdate0 =  createShipmentWithAlertResult0.response.data.shipment_journey.package_journey_segments[0].alerts[0].id

    Given path '/alerts/' + segmentAlertIdToUpdate0
    Given param dismissed = true
    And header Authorization = 'Bearer ' + newTokenA
    When method PATCH
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Happy path: Dismiss shipment alert, user B (partner B) tries to dismiss shipment alert created from partner B
    * def createShipmentWithAlertResultB = call read('classpath:features/shipment/addSingleWithAlerts.feature') {singleRQPath : '#(shipmentWithAlertJsonB)'}
    * def segmentAlertIdToUpdateB =  createShipmentWithAlertResultB.response.data.shipment_journey.package_journey_segments[0].alerts[0].id

    Given path '/alerts/' + segmentAlertIdToUpdateB
    Given param dismissed = true
    And header Authorization = 'Bearer ' + newTokenB
    When method PATCH
    Then status 204

  @Permissions
  Scenario: Unhappy path: Dismiss shipment alert, user B (partner B) tries to dismiss shipment alert created from partner A
    * def createShipmentWithAlertResultA = call read('classpath:features/shipment/addSingleWithAlerts.feature') {singleRQPath : '#(shipmentWithAlertJsonA)'}
    * def segmentAlertIdToUpdateA =  createShipmentWithAlertResultA.response.data.shipment_journey.package_journey_segments[0].alerts[0].id

    Given path '/alerts/' + segmentAlertIdToUpdateA
    Given param dismissed = true
    And header Authorization = 'Bearer ' + newTokenB
    When method PATCH
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Happy path: Dismiss shipment alert, user A1 (extra partner A) tries to dismiss shipment alert created from partner A
    * def createShipmentWithAlertResultA = call read('classpath:features/shipment/addSingleWithAlerts.feature') {singleRQPath : '#(shipmentWithAlertJsonA)'}
    * def segmentAlertIdToUpdateA =  createShipmentWithAlertResultA.response.data.shipment_journey.package_journey_segments[0].alerts[0].id

    Given path '/alerts/' + segmentAlertIdToUpdateA
    Given param dismissed = true
    And header Authorization = 'Bearer ' + newTokenA1
    When method PATCH
    Then status 204

  @Permissions
  Scenario: Unhappy path: Dismiss shipment alert, user A1 (extra partner A) tries to dismiss shipment alert created from partner B
    * def createShipmentWithAlertResultB = call read('classpath:features/shipment/addSingleWithAlerts.feature') {singleRQPath : '#(shipmentWithAlertJsonB)'}
    * def segmentAlertIdToUpdateB =  createShipmentWithAlertResultB.response.data.shipment_journey.package_journey_segments[0].alerts[0].id

    Given path '/alerts/' + segmentAlertIdToUpdateB
    Given param dismissed = true
    And header Authorization = 'Bearer ' + newTokenA1
    When method PATCH
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Unhappy path: Dismiss shipment alert, user A1 (extra partner A) tries to dismiss shipment alert created from company (no partner)
    * def createShipmentWithAlertResult0 = call read('classpath:features/shipment/addSingleWithAlerts.feature') {singleRQPath : '#(shipmentWithAlertJson0)'}
    * def segmentAlertIdToUpdate0 =  createShipmentWithAlertResult0.response.data.shipment_journey.package_journey_segments[0].alerts[0].id

    Given path '/alerts/' + segmentAlertIdToUpdate0
    Given param dismissed = true
    And header Authorization = 'Bearer ' + newTokenA1
    When method PATCH
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Unhappy path: Dismiss shipment alert, company user tries to dismiss shipment alert created from partner A
    * def createShipmentWithAlertResultA = call read('classpath:features/shipment/addSingleWithAlerts.feature') {singleRQPath : '#(shipmentWithAlertJsonA)'}
    * def segmentAlertIdToUpdateA =  createShipmentWithAlertResultA.response.data.shipment_journey.package_journey_segments[0].alerts[0].id

    Given path '/alerts/' + segmentAlertIdToUpdateA
    Given param dismissed = true
    And header Authorization = 'Bearer ' + newTokenComp0
    When method PATCH
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Happy path: Dismiss shipment alert, company user tries to dismiss shipment alert created from company (no partner)
    * def createShipmentWithAlertResult0 = call read('classpath:features/shipment/addSingleWithAlerts.feature') {singleRQPath : '#(shipmentWithAlertJson0)'}
    * def segmentAlertIdToUpdate0 =  createShipmentWithAlertResult0.response.data.shipment_journey.package_journey_segments[0].alerts[0].id

    Given path '/alerts/' + segmentAlertIdToUpdate0
    Given param dismissed = true
    And header Authorization = 'Bearer ' + newTokenComp0
    When method PATCH
    Then status 204

  @Permissions
  Scenario: Happy path: Dismiss shipment alert, company user (extra partner A) tries to dismiss shipment alert created from partner A
    * def createShipmentWithAlertResultA = call read('classpath:features/shipment/addSingleWithAlerts.feature') {singleRQPath : '#(shipmentWithAlertJsonA)'}
    * def segmentAlertIdToUpdateA =  createShipmentWithAlertResultA.response.data.shipment_journey.package_journey_segments[0].alerts[0].id

    Given path '/alerts/' + segmentAlertIdToUpdateA
    Given param dismissed = true
    And header Authorization = 'Bearer ' + newTokenComp1
    When method PATCH
    Then status 204

  @Permissions
  Scenario: Unhappy path: Dismiss shipment alert, company user (extra partner A) tries to dismiss shipment alert created from partner B
    * def createShipmentWithAlertResultB = call read('classpath:features/shipment/addSingleWithAlerts.feature') {singleRQPath : '#(shipmentWithAlertJsonB)'}
    * def segmentAlertIdToUpdateB =  createShipmentWithAlertResultB.response.data.shipment_journey.package_journey_segments[0].alerts[0].id

    Given path '/alerts/' + segmentAlertIdToUpdateB
    Given param dismissed = true
    And header Authorization = 'Bearer ' + newTokenComp1
    When method PATCH
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Happy path: Dismiss shipment alert, company user (extra partner A) tries to dismiss shipment alert created from company (no partner)
    * def createShipmentWithAlertResult0 = call read('classpath:features/shipment/addSingleWithAlerts.feature') {singleRQPath : '#(shipmentWithAlertJson0)'}
    * def segmentAlertIdToUpdate0 =  createShipmentWithAlertResult0.response.data.shipment_journey.package_journey_segments[0].alerts[0].id

    Given path '/alerts/' + segmentAlertIdToUpdate0
    Given param dismissed = true
    And header Authorization = 'Bearer ' + newTokenComp1
    When method PATCH
    Then status 204

  @Permissions
  Scenario: Happy path: Cancel shipment, user A (partner A) tries to cancel shipment created from partner A
    Given path '/shipments/cancel/' + shipmentIdA
    And header Authorization = 'Bearer ' + newTokenA
    When method PATCH
    Then status 200

  @Permissions
  Scenario: Unhappy path: Cancel shipment, user A (partner A) tries to cancel shipment created from partner B
    Given path '/shipments/cancel/' + shipmentIdB
    And header Authorization = 'Bearer ' + newTokenA
    When method PATCH
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Unhappy path: Cancel shipment, user A (partner A) tries to cancel shipment created from company (no partner)
    Given path '/shipments/cancel/' + shipmentId0
    And header Authorization = 'Bearer ' + newTokenA
    When method PATCH
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Happy path: Cancel shipment, user B (partner B) tries to cancel shipment created from partner B
    Given path '/shipments/cancel/' + shipmentIdB
    And header Authorization = 'Bearer ' + newTokenB
    When method PATCH
    Then status 200

  @Permissions
  Scenario: Unhappy path: Cancel shipment, user B (partner B) tries to cancel shipment created from partner A
    Given path '/shipments/cancel/' + shipmentId0
    And header Authorization = 'Bearer ' + newTokenA
    When method PATCH
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Happy path: Cancel shipment, user A1 (extra partner A) tries to cancel shipment created from partner A
    Given path '/shipments/cancel/' + shipmentIdA
    And header Authorization = 'Bearer ' + newTokenA1
    When method PATCH
    Then status 200

  @Permissions
  Scenario: Unhappy path: Cancel shipment, user A1 (extra partner A) tries to cancel shipment created from partner B
    Given path '/shipments/cancel/' + shipmentIdB
    And header Authorization = 'Bearer ' + newTokenA1
    When method PATCH
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Unhappy path: Cancel shipment, user A1 (extra partner A) tries to cancel shipment created from company (no partner)
    Given path '/shipments/cancel/' + shipmentId0
    And header Authorization = 'Bearer ' + newTokenA1
    When method PATCH
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Unhappy path: Cancel shipment, company user tries to cancel shipment created from partner A
    Given path '/shipments/cancel/' + shipmentIdA
    And header Authorization = 'Bearer ' + newTokenComp0
    When method PATCH
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Happy path: Cancel shipment, company user (extra partner A) tries to cancel shipment created from company (no partner)
    Given path '/shipments/cancel/' + shipmentId0
    And header Authorization = 'Bearer ' + newTokenComp1
    When method PATCH
    Then status 200

  @Permissions
  Scenario: Happy path: Cancel shipment, company user (extra partner A) tries to cancel shipment created from partner A
    Given path '/shipments/cancel/' + shipmentIdA
    And header Authorization = 'Bearer ' + newTokenComp1
    When method PATCH
    Then status 200

  @Permissions
  Scenario: Unhappy path: Cancel shipment, company user (extra partner A) tries to cancel shipment created from partner B
    Given path '/shipments/cancel/' + shipmentIdB
    And header Authorization = 'Bearer ' + newTokenComp1
    When method PATCH
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @Permissions
  Scenario: Happy path: Cancel shipment, company user (extra partner A) tries to cancel shipment created from company (no partner)
    Given path '/shipments/cancel/' + shipmentId0
    And header Authorization = 'Bearer ' + newTokenComp1
    When method PATCH
    Then status 200