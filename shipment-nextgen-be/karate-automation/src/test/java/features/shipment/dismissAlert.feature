Feature: Dismiss Alert Feature

  Background:
    * url baseUrl
    * def bearer = token

    * def createShipmentResult = call read('classpath:features/shipment/addSingleWithAlerts.feature')
    * def shipmentJourneyAlertIdToUpdate =  createShipmentResult.response.data.shipment_journey.alerts[0].id
    * def segmentAlertIdToUpdate =  createShipmentResult.response.data.shipment_journey.package_journey_segments[0].alerts[0].id

  @AlertDismiss @SegmentRegression
  Scenario: Dismiss shipment journey alert
    Given path '/alerts/' + shipmentJourneyAlertIdToUpdate
    Given param dismissed = true
    And header Authorization = 'Bearer '+ bearer
    When method PATCH
    Then status 204

  @AlertDismiss @SegmentRegression
  Scenario: Dismiss package journey segment alert
    Given path '/alerts/' + segmentAlertIdToUpdate
    Given param dismissed = true
    And header Authorization = 'Bearer '+ bearer
    When method PATCH
    Then status 204

  @AlertDismiss @SegmentRegression
  Scenario: Dismiss unknown alert
    Given path '/alerts/' + utils.uuid()
    Given param dismissed = true
    And header Authorization = 'Bearer '+ bearer
    When method PATCH
    Then status 404
