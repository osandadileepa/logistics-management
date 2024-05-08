Feature: No Location Permissions

  Background:
    * url baseUrl

    * def workingBearer = token

    * def userCredentials = 'classpath:session/json/no_location_coverage.json'
    * def workingUserCredentials = 'classpath:session/json/shipments_edit.json'
    * def newSession = callonce read('classpath:session/create.feature') {userCredentials : '#(userCredentials)'}
    * def bearer = newSession.response.data.token

    * def shipmentDetails = callonce read('classpath:features/shipment/get.feature')
    * def shipment = shipmentDetails.response.data
    * def shipmentId = shipment.id
    * print shipmentId
    * def shipment_tracking_id = shipment.shipment_tracking_id
    * def shipmentJourneyIdToUpdate = shipment.shipment_journey.journey_id
    * print shipmentJourneyIdToUpdate
    * def dimensionId = shipment.shipment_package.dimension.id
    * def oldLength = shipment.shipment_package.dimension.length
    * def oldWidth = shipment.shipment_package.dimension.width
    * def oldHeight = shipment.shipment_package.dimension.height
    * def oldGrossWeight = shipment.shipment_package.dimension.gross_weight

    * def updateRequest = read('classpath:features/shipment/json/updateRQ.json')
    * updateRequest.data.id = shipmentId

    * def requestForUpdate = read('classpath:features/shipment/json/updateJourneyRQ.json')
    * requestForUpdate.data.shipment_id = shipmentId
    * requestForUpdate.data.journey_id = shipmentJourneyIdToUpdate
    * requestForUpdate.data.package_journey_segments[0].status = 'PLANNED'
    * print requestForUpdate

    * def requestForUpdateDimension = read('classpath:features/shipment/json/updatePackageDimensionRQ.json')
    * requestForUpdateDimension.data.length = 12.123
    * requestForUpdateDimension.data.width = 12.123
    * requestForUpdateDimension.data.height = 12.123
    * requestForUpdateDimension.data.gross_weight = 12.123
    * print requestForUpdateDimension

    * def createShipmentWithAlertJson = 'classpath:features/shipment/json/single/shipment-with-alerts.json'
    * def createShipmentWithAlertResult = callonce read('classpath:features/shipment/get.feature') {createRQ : '#(createShipmentWithAlertJson)'}
    * def journeyAlertIdToUpdate =  createShipmentWithAlertResult.response.data.shipment_journey.alerts[0].id
    * def segmentAlertIdToUpdate =  createShipmentWithAlertResult.response.data.shipment_journey.package_journey_segments[0].alerts[0].id

    * def createShipmentResultForMilestone = callonce read('classpath:features/shipment/addSingleWithValidDetails.feature')
    * def requestForUpdateMilestoneAdditionalInfo = read('classpath:features/shipment/json/updateShipmentMilestoneAdditionalInfoRQ.json')
    * def shipment_tracking_id_for_milestone = createShipmentResultForMilestone.data.shipment_tracking_id
    * requestForUpdateMilestoneAdditionalInfo.data.shipment_id = shipmentId
    * print requestForUpdateMilestoneAdditionalInfo

    * def requestForMilestoneUpdate = read('classpath:features/shipment/json/milestone/updateMilestoneRQ.json')
    * requestForMilestoneUpdate.data.milestone_code = requestForUpdateMilestoneAdditionalInfo.data.milestone_code

  @Regression
  @LocationPermissions
  Scenario: Unhappy path: User with no location permission attempts to retrieve a shipment
    Given path '/shipments/' + shipmentId
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 403

  @Regression
  @LocationPermissions
  Scenario: Unhappy path: User with no location permission attempts to update a shipment
    Given path '/shipments'
    And header Authorization = 'Bearer '+ bearer
    And request updateRequest
    When method PUT
    Then status 403

  @Regression
  @LocationPermissions
  Scenario: Unhappy path: User with no location permission attempts to retrieve a shipment journey
    Given path '/shipments/' + shipmentId + '/shipment_journey'
    And header Authorization = 'Bearer ' + bearer
    When method GET
    Then status 403

  @Regression
  @LocationPermissions
  Scenario: Unhappy path: User with no location permission attempts to update a shipment journey
    Given path '/shipments/shipment_journey'
    And header Authorization = 'Bearer '+ bearer
    And request requestForUpdate
    When method PUT
    Then status 403

  @Regression
  @LocationPermissions
  Scenario: Unhappy path: User with no location permission attempts to cancel a shipment
    Given path '/shipments/cancel/' + shipmentId
    And header Authorization = 'Bearer '+ bearer
    When method PATCH
    Then status 403

  @Regression
  @LocationPermissions
  Scenario: Unhappy path: User with no location permission attempts to retrieve a package dimension
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipment_tracking_id + '/package-dimension')
    And header Authorization = 'Bearer ' + bearer
    When method GET
    Then status 403

  @Regression
  @LocationPermissions
  Scenario: Unhappy path: User with no location permission attempts to update a package dimension
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipment_tracking_id + '/package-dimension')
    And header Authorization = 'Bearer '+ bearer
    And request requestForUpdateDimension
    When method PUT
    Then status 403

  @Regression
  @LocationPermissions
  Scenario: Unhappy path: User with no location permission attempts to dismiss a journey alert
    Given path '/alerts/' + journeyAlertIdToUpdate
    Given param dismissed = true
    And header Authorization = 'Bearer '+ bearer
    When method PATCH
    Then status 403

  @Regression
  @LocationPermissions
  Scenario: Unhappy path: User with no location permission attempts to dismiss a segment alert
    Given path '/alerts/' + segmentAlertIdToUpdate
    Given param dismissed = true
    And header Authorization = 'Bearer '+ bearer
    When method PATCH
    Then status 403

  @Regression
  @LocationPermissions
  Scenario: Unhappy path: User with no location permission attempts to update a milestone with additional info
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipment_tracking_id_for_milestone + '/milestone-and-additional-info')
    And header Authorization = 'Bearer '+ bearer
    And request requestForUpdateMilestoneAdditionalInfo
    When method PATCH
    Then status 403

  @Regression
  @LocationPermissions
  Scenario: Unhappy path: User with no location permission attempts to partial update a milestone
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipment_tracking_id_for_milestone + '/milestone-and-additional-info')
    And header Authorization = 'Bearer '+ workingBearer
    And request requestForUpdateMilestoneAdditionalInfo
    When method PATCH
    Then status 200

    * def response = $
    * print response
    * requestForMilestoneUpdate.data.shipment_id = response.data.shipment_id

    Given url utils.decodeUrl(baseUrl + '/milestones/partial-update')
    And header Authorization = 'Bearer '+ bearer
    And request requestForMilestoneUpdate
    When method PATCH
    Then status 403