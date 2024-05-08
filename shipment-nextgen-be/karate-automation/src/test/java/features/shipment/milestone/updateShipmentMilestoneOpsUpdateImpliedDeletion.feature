Feature: Update Shipment Milestone Ops Update (Shipment implied deleted)

  Background:
    * url baseUrl
    * def bearer = token

    * def omPayload = 'classpath:features/shipment/json/omPayload.json'
    * def createShipmentResult = callonce read('classpath:features/shipment/addSingleFromOMPayload.feature') {singleRQPath : '#(omPayload)'}
    * def omPayloadSingleShipment = 'classpath:features/shipment/json/omPayloadSingleShipment.json'
    * def updateShipmentResult = callonce read('classpath:features/shipment/addSingleFromOMPayloadCommon.feature') {singleRQPath : '#(omPayloadSingleShipment)'}
    * def shipment_tracking_id = updateShipmentResult.response.data[0].shipment_tracking_id
    * print shipment_tracking_id

    * def requestForUpdate = read('classpath:features/shipment/json/updateShipmentMilestoneAdditionalInfoRQ.json')
    * print requestForUpdate

  @ShipmentMilestoneOpsUpdate @Regression
  Scenario: Update Shipment Milestone Additional Info
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipment_tracking_id + '/milestone-and-additional-info')
    And header Authorization = 'Bearer '+ bearer
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