Feature: Update a Shipment Feature

  Background:
    # Call the create shipment feature then use the returned shipmentTrackingID
    # to call the update shipment API
    * url baseUrl
    * def bearer = token

    * def createShipmentResult = callonce read('classpath:features/shipment/addSingle.feature')
    * def shipmentTrackingIdToUpdate = createShipmentResult.response.data.id

    * def requestForUpdate = read('classpath:features/shipment/json/updateRQ.json')
    * requestForUpdate.data.id = shipmentTrackingIdToUpdate
    * requestForUpdate.data.shipment_tracking_id = createShipmentResult.data.shipment_tracking_id
    * requestForUpdate.data.order.id = createShipmentResult.data.order.id
    * requestForUpdate.data.shipment_package.id = createShipmentResult.response.data.shipment_package.id
    * requestForUpdate.data.shipment_journey.journey_id = createShipmentResult.response.data.shipment_journey.journey_id
    * requestForUpdate.data.sender.name = 'senderNameUpdated'
    * requestForUpdate.data.sender.email = 'senderEmail@updated.com'
    * requestForUpdate.data.sender.contact_number = '1111144444'
    * requestForUpdate.data.consignee.name = 'consigneeUpdated'
    * requestForUpdate.data.consignee.email = 'consigneeEmail@updated.com'
    * requestForUpdate.data.consignee.contact_number = '33334444'
    * requestForUpdate.data.consignee.contact_code = '+40'
    * requestForUpdate.data.pick_up_location = 'pick_up_location_updated'
    * requestForUpdate.data.delivery_location = 'delivery_location_updated'
    * requestForUpdate.data.shipment_package.type = 'package_type_updated'
    * requestForUpdate.data.shipment_package.currency = 'ABC'
    * requestForUpdate.data.shipment_package.dimension.measurement_unit = 'IMPERIAL'
    * requestForUpdate.data.shipment_package.dimension.length = 1.123
    * requestForUpdate.data.shipment_package.dimension.width = 2.345
    * requestForUpdate.data.shipment_package.dimension.height = 3.456
    * requestForUpdate.data.shipment_package.dimension.gross_weight = 4.567
    * requestForUpdate.data.shipment_package.dimension.volume_weight = 5.678
    * requestForUpdate.data.shipment_package.dimension.chargeable_weight = 6.789
    * requestForUpdate.data.notes = 'notes_updated'
    * requestForUpdate.data.status = 'COMPLETED'
    * requestForUpdate.data.origin.country = 'origin_country_updated'
    * requestForUpdate.data.origin.state = 'origin_state_updated'
    * requestForUpdate.data.origin.city = 'origin_city_updated'
    * requestForUpdate.data.origin.line1 = 'origin_line1_updated'
    * requestForUpdate.data.origin.line2 = 'origin_line2_updated'
    * requestForUpdate.data.origin.line3 = 'origin_line3_updated'
    * requestForUpdate.data.origin.country_id = 'origin_country_updated'
    * requestForUpdate.data.origin.state_id = 'origin_state_updated'
    * requestForUpdate.data.origin.city_id = 'origin_city_updated'
    * requestForUpdate.data.origin.country_name = 'origin_country_updated'
    * requestForUpdate.data.origin.state_name = 'origin_state_updated'
    * requestForUpdate.data.origin.city_name = 'origin_city_updated'
    * requestForUpdate.data.destination.country = 'destination_country_updated'
    * requestForUpdate.data.destination.state = 'destination_state_updated'
    * requestForUpdate.data.destination.city = 'destination_city_updated'
    * requestForUpdate.data.destination.line1 = 'destination_line1_updated'
    * requestForUpdate.data.destination.line2 = 'destination_line2_updated'
    * requestForUpdate.data.destination.line3 = 'destination_line3_updated'
    * requestForUpdate.data.destination.country_id = 'destination_country_updated'
    * requestForUpdate.data.destination.state_id = 'destination_state_updated'
    * requestForUpdate.data.destination.city_id = 'destination_city_updated'
    * requestForUpdate.data.destination.country_name = 'destination_country_updated'
    * requestForUpdate.data.destination.state_name = 'destination_state_updated'
    * requestForUpdate.data.destination.city_name = 'destination_city_updated'
    * requestForUpdate.data.instructions[0].id = "49c22f59-00ff-4b26-945d-9bda37af2f3e"
    * requestForUpdate.data.instructions[0].label = "label_updated"
    * requestForUpdate.data.instructions[0].source = "source_updated"
    * requestForUpdate.data.instructions[0].value = "value_updated"
    * requestForUpdate.data.instructions[0].apply_to = "DELIVERY"
    * requestForUpdate.data.instructions[0].created_at = "2023-04-19T09:10:43.614Z"
    * requestForUpdate.data.instructions[0].updated_at = "2023-04-19T09:10:43.614Z"
    * print requestForUpdate


  @ShipmentUpdate @Regression
  Scenario: Update Shipment
    Given path '/shipments'
    And header Authorization = 'Bearer '+ bearer
    And request requestForUpdate
    When method PUT
    Then status 200

    * def response = $
    * print response

    * match response.data.id == shipmentTrackingIdToUpdate
    * match requestForUpdate.data.sender.name == response.data.sender.name
    * match requestForUpdate.data.sender.email == response.data.sender.email
    * match requestForUpdate.data.sender.contact_number == response.data.sender.contact_number
    * match requestForUpdate.data.consignee.name == response.data.consignee.name
    * match requestForUpdate.data.consignee.email == response.data.consignee.email
    * match requestForUpdate.data.consignee.contact_number == response.data.consignee.contact_number
    * match requestForUpdate.data.consignee.contact_code ==	response.data.consignee.contact_code
    * match requestForUpdate.data.pick_up_location == response.data.pick_up_location
    * match requestForUpdate.data.delivery_location == response.data.delivery_location
    * match requestForUpdate.data.shipment_package.type == response.data.shipment_package.type
    * match requestForUpdate.data.shipment_package.currency == response.data.shipment_package.currency
    * match requestForUpdate.data.shipment_package.dimension.measurement_unit == response.data.shipment_package.dimension.measurement_unit
    * match requestForUpdate.data.shipment_package.dimension.length == response.data.shipment_package.dimension.length
    * match requestForUpdate.data.shipment_package.dimension.width == response.data.shipment_package.dimension.width
    * match requestForUpdate.data.shipment_package.dimension.height == response.data.shipment_package.dimension.height
    * match requestForUpdate.data.shipment_package.dimension.gross_weight == response.data.shipment_package.dimension.gross_weight
    * match requestForUpdate.data.shipment_package.dimension.volume_weight == response.data.shipment_package.dimension.volume_weight
    * match requestForUpdate.data.shipment_package.dimension.chargeable_weight == response.data.shipment_package.dimension.chargeable_weight
    * match requestForUpdate.data.notes == response.data.notes
    * match requestForUpdate.data.origin.line1 == response.data.origin.line1
    * match requestForUpdate.data.origin.line2 == response.data.origin.line2
    * match requestForUpdate.data.origin.line3 == response.data.origin.line3
    * match requestForUpdate.data.origin.country_id == response.data.origin.country_id
    * match requestForUpdate.data.origin.state_id == response.data.origin.state_id
    * match requestForUpdate.data.origin.city_id == response.data.origin.city_id
    * match requestForUpdate.data.origin.country_name == response.data.origin.country_name
    * match requestForUpdate.data.origin.state_name == response.data.origin.state_name
    * match requestForUpdate.data.origin.city_name == response.data.origin.city_name
    * match requestForUpdate.data.destination.line1 == response.data.destination.line1
    * match requestForUpdate.data.destination.line2 == response.data.destination.line2
    * match requestForUpdate.data.destination.line3 == response.data.destination.line3
    * match requestForUpdate.data.destination.country_id ==	response.data.destination.country_id
    * match requestForUpdate.data.destination.state_id == response.data.destination.state_id
    * match requestForUpdate.data.destination.city_id == response.data.destination.city_id
    * match requestForUpdate.data.destination.country_name == response.data.destination.country_name
    * match requestForUpdate.data.destination.state_name ==	response.data.destination.state_name
    * match requestForUpdate.data.destination.city_name == response.data.destination.city_name
    * match requestForUpdate.data.instructions[0].id == response.data.instructions[0].id
    * match requestForUpdate.data.instructions[0].label == response.data.instructions[0].label
    * match requestForUpdate.data.instructions[0].source == response.data.instructions[0].source
    * match requestForUpdate.data.instructions[0].value == response.data.instructions[0].value
    * match requestForUpdate.data.instructions[0].apply_to == response.data.instructions[0].apply_to
    * match requestForUpdate.data.instructions[0].created_at == response.data.instructions[0].created_at
    * match requestForUpdate.data.instructions[0].updated_at == response.data.instructions[0].updated_at

