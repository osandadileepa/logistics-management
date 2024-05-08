Feature: Update Shipment Milestone via API

  Background:
    * url baseUrl
    * def bearer = token

    * def createShipmentResult = call read('classpath:features/shipment/addSingleWithValidDetails.feature')
    * print createShipmentResult.response
    * def requestForUpdate = read('classpath:features/shipment/json/updateShipmentMilestoneAdditionalInfoRQ.json')
    * def shipment_tracking_id = createShipmentResult.response.data.shipment_tracking_id
    * def external_order_id = createShipmentResult.response.data.external_order_id
    * def internal_order_id = createShipmentResult.response.data.order.order_id_label
    * def order_id_from_apig = karate.get('external_order_id', internal_order_id)
    * def milestone_code = requestForUpdate.data.milestone_code
    * requestForUpdate.data.external_order_id = order_id_from_apig
    * print requestForUpdate

    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipment_tracking_id + '/milestone-and-additional-info')
    And header Authorization = 'Bearer '+ bearer
    And request requestForUpdate
    When method PATCH
    Then status 200

    * def response = $
    * print response
    * def requestForUpdate = read('classpath:features/shipment/json/milestone/updateMilestoneRQ.json')
    * def shipment_id = response.data.shipment_id

    * requestForUpdate.data.shipment_id = shipment_id
    * requestForUpdate.data.external_order_id = order_id_from_apig
    * requestForUpdate.data.milestone_code = milestone_code
    * print requestForUpdate

  @UpdateMilestone @Regression
  Scenario: Update Partial Shipment Milestone
    Given url utils.decodeUrl(baseUrl + '/milestones/partial-update')
    And header Authorization = 'Bearer '+ bearer
    And request requestForUpdate
    When method PATCH
    Then status 200

    * def response = $
    * print response

    * match response.data.milestone_coordinates == '#notnull'
    * match response.data.additional_info.remarks == '#notnull'
    * match response.data.failed_reason == '#notnull'
    * match response.data.failed_reason_code == '#notnull'
    * match response.data.eta == '#notnull'
    * match response.data.receiver_name == '#notnull'
    * match response.data.sender_name == '#notnull'
    * match response.data.vehicle_number == '#notnull'
    * match response.data.vehicle_type == '#notnull'
    * match response.data.vehicle_id == '#notnull'
    * match response.data.driver_phone_number == '#notnull'
    * match response.data.driver_phone_code == '#notnull'
    * match response.data.driver_name == '#notnull'
    * match response.data.driver_id == '#notnull'
    * match response.data.driver_email == '#notnull'
    * match response.data.hub_id == '#notnull'
    * match response.data.job_type == '#notnull'
    * match response.data.service_type == '#notnull'
    * match response.data.partner_id == '#notnull'
    * match response.data.segment_id != '1'
    * match response.data.user_id == '#notnull'
    * match response.data.to_city_id == '#notnull'
    * match response.data.to_state_id == '#notnull'
    * match response.data.to_country_id == '#notnull'
    * match response.data.to_location_id == '#notnull'
    * match response.data.from_city_id == '#notnull'
    * match response.data.from_state_id == '#notnull'
    * match response.data.from_country_id == '#notnull'
    * match response.data.from_location_id == '#notnull'
    * match response.data.source == '#notnull'


