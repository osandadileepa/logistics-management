Feature: Update Shipment Milestone via API With Order Having 2 Shipment

  Background:
    * url baseUrl
    * def bearer = token

    * def createShipmentsFromOrderResult = callonce read('classpath:features/shipment/addSingleFromOMPayload.feature')
    * def shipment_tracking_id_1 = createShipmentsFromOrderResult.data[0].shipment_tracking_id
    * def shipment_tracking_id_2 = createShipmentsFromOrderResult.data[1].shipment_tracking_id
    * def requestForUpdate = read('classpath:features/shipment/json/deliverySuccessMilestoneAdditionalInfoRQ.json')
    * requestForUpdate.data.milestone_time = utils.getOffsetDateTimePlusDays(1)


  @UpdateMilestone @Regression
  Scenario: 2 Shipment Should Have Milestone Update before proceeding to update segment status
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipment_tracking_id_1 + '/milestone-and-additional-info')
    And header Authorization = 'Bearer '+ bearer
    And request requestForUpdate
    When method PATCH
    Then status 200

    Given url baseUrl + '/shipments?shipment_tracking_id='+ shipment_tracking_id_1
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * match response.data.shipment_journey.package_journey_segments[0].status == 'PLANNED'

    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipment_tracking_id_2 + '/milestone-and-additional-info')
    And header Authorization = 'Bearer '+ bearer
    And request requestForUpdate
    When method PATCH
    Then status 200

    Given url baseUrl + '/shipments?shipment_tracking_id='+ shipment_tracking_id_1
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * match response.data.shipment_journey.package_journey_segments[0].status == 'COMPLETED'