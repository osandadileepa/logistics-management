Feature: Update Shipment Milestone Ops Update

  Background:
    * url baseUrl
    * def bearer = token

    * def createShipmentResult = callonce read('classpath:features/shipment/addSingleWithValidDetails.feature')
    * def requestForUpdate = read('classpath:features/shipment/json/updateShipmentMilestoneAdditionalInfoRQ.json')
    * def shipment_tracking_id = createShipmentResult.data.shipment_tracking_id
    * print requestForUpdate

  @ShipmentMilestoneOpsUpdate @Regression
  Scenario: Update Shipment Milestone Additional Info
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipment_tracking_id + '/milestone-and-additional-info')
    And header Authorization = 'Bearer '+ bearer
    * requestForUpdate.data.milestone_code = '1117'
    * requestForUpdate.data.milestone_time = utils.getFormattedOffsetDateTimeNow()
    And request requestForUpdate
    When method PATCH
    Then status 200

    * def response = $
    * print response

    * match response.data.shipment_id == '#present'
    * match response.data.shipment_tracking_id == shipment_tracking_id
    * match response.data.notes == requestForUpdate.data.notes
    * match response.data.attachments[0].file_name == '#present'
    * match response.data.attachments[0].file_url == '#present'
    * match response.data.attachments[0].file_size == '#present'
    * match response.data.attachments[0].file_timestamp == requestForUpdate.data.milestone_time
    * match response.data.attachments[1].file_name == '#present'
    * match response.data.attachments[1].file_url == '#present'
    * match response.data.attachments[1].file_size == '#present'
    * match response.data.attachments[1].file_timestamp == requestForUpdate.data.milestone_time
    * match response.data.attachments[2].file_name == '#present'
    * match response.data.attachments[2].file_url == '#present'
    * match response.data.attachments[2].file_size == '#present'
    * match response.data.attachments[2].file_timestamp == requestForUpdate.data.milestone_time
    * match response.data.attachments[3].file_name == '#present'
    * match response.data.attachments[3].file_url == '#present'
    * match response.data.attachments[3].file_size == '#present'
    * match response.data.attachments[3].file_timestamp == requestForUpdate.data.milestone_time
    * match response.data.previous_milestone_name == 'Order Booked'
    * match response.data.current_milestone_name == requestForUpdate.data.milestone_name
    * match response.data.current_milestone_id == '#present'
    * match response.data.updated_by == '#present'
    * match response.data.users_location.location_id =='#present'
    * match response.data.users_location.location_facility_name =='#present'
    * match response.data.milestone_time == '#present'

  @ShipmentMilestoneAdditionalInfoUpdate @Regression
  Scenario: Update Shipment Milestone Additional Info with invalid request
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipment_tracking_id + '/milestone-and-additional-info')
    And header Authorization = 'Bearer '+ bearer
    * requestForUpdate.data.milestone_name = null
    * requestForUpdate.data.milestone_code = null
    * requestForUpdate.data.shipment_attachments = null
    And request requestForUpdate
    When method PATCH
    Then status 400

    * def response = $
    * print response

    * match response.message == '#present'
    * match response.message == 'There is a validation error in your request'

  @ShipmentMilestoneAdditionalInfoUpdate @Regression
  Scenario: Update Shipment Milestone Additional Info with non-existing shipment tracking id
    * def shipment_tracking_id = 'INVALID_SHIPMENT_TRACKING_ID'
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipment_tracking_id + '/milestone-and-additional-info')
    And header Authorization = 'Bearer '+ bearer
    And request requestForUpdate
    When method PATCH
    Then status 404

    * def response = $
    * print response
    