Feature: Create a Single Shipment with segment Feature and updates

  Background:
    * url baseUrl
    * def requestPath = karate.get('singleRQPath', 'classpath:features/shipment/json/single/shipment-with-segment.json')
    * def updatedCreateRequest = read(requestPath)
    * updatedCreateRequest.data.shipment_tracking_id = 'SHP' + utils.uuid().substring(0, 12)
    * updatedCreateRequest.data.order.id = utils.uuid().substring(0, 10)
    * updatedCreateRequest.data.order.order_id_label = utils.uuid().substring(0, 10)
    * def segments = updatedCreateRequest.data.shipment_journey.package_journey_segments
    * def STATUS_PLANNED = 'PLANNED'
    * def STATUS_IN_PROGRESS = 'IN_PROGRESS'
    * def STATUS_COMPLETED = 'COMPLETED'

    # Create Shipment with 5 Segments
  @TestShipmentCreateWithSegmentUpdates @SegmentRegression
  Scenario: Create segment with updates
    * def bearer = token
    * def shipmentTrackingID = utils.uuid()
    Given path '/shipments'
    And header Authorization = 'Bearer '+ bearer
    And request updatedCreateRequest
    When method POST
    Then status 200

    * def response = $
    * def data = response.data
    * match data.shipment_journey.package_journey_segments == '#[3]'
    * match data.shipment_journey.package_journey_segments[0].status == STATUS_PLANNED
    * match data.shipment_journey.package_journey_segments[1].status == STATUS_PLANNED
    * match data.shipment_journey.package_journey_segments[2].status == STATUS_PLANNED

    # Update First segment status to In Progress
    * def bearer = token
    * def shipmentTrackingID = utils.uuid()
    Given path '/shipments'
    And header Authorization = 'Bearer '+ bearer
    And segments[0].status = STATUS_IN_PROGRESS
    And request updatedCreateRequest
    When method POST
    Then status 200


    * def response = $
    * def data = response.data
    * match data.shipment_journey.package_journey_segments[0].status == STATUS_IN_PROGRESS
    * match data.shipment_journey.package_journey_segments[1].status == STATUS_PLANNED
    * match data.shipment_journey.package_journey_segments[2].status == STATUS_PLANNED

    # Update First segment status to Completed
    * def bearer = token
    * def shipmentTrackingID = utils.uuid()
    Given path '/shipments'
    And header Authorization = 'Bearer '+ bearer
    And segments[0].status = STATUS_COMPLETED
    And request updatedCreateRequest
    When method POST
    Then status 200

    * def response = $
    * print response

    * def data = response.data
    * match data.shipment_journey.package_journey_segments[0].status == STATUS_COMPLETED
    * match data.shipment_journey.package_journey_segments[1].status == STATUS_PLANNED
    * match data.shipment_journey.package_journey_segments[2].status == STATUS_PLANNED

    # Update First and Second segments status to Completed, In Progress
    * def bearer = token
    * def shipmentTrackingID = utils.uuid()
    Given path '/shipments'
    And header Authorization = 'Bearer '+ bearer
    And segments[0].status = STATUS_COMPLETED
    And segments[1].status = STATUS_IN_PROGRESS
    And request updatedCreateRequest
    When method POST
    Then status 200

    * def response = $
    * def data = response.data
    * match data.shipment_journey.package_journey_segments[0].status == STATUS_COMPLETED
    * match data.shipment_journey.package_journey_segments[1].status == STATUS_IN_PROGRESS
    * match data.shipment_journey.package_journey_segments[2].status == STATUS_PLANNED

    # Update Segments status
    * def bearer = token
    * def shipmentTrackingID = utils.uuid()
    Given path '/shipments'
    And header Authorization = 'Bearer '+ bearer
    And segments[0].status = STATUS_COMPLETED
    And segments[1].status = STATUS_COMPLETED
    And request updatedCreateRequest
    When method POST
    Then status 200

    * def response = $
    * def data = response.data
    * match data.shipment_journey.package_journey_segments[0].status == STATUS_COMPLETED
    * match data.shipment_journey.package_journey_segments[1].status == STATUS_COMPLETED
    * match data.shipment_journey.package_journey_segments[2].status == STATUS_PLANNED

    # Update All segments to completed
    * def bearer = token
    * def shipmentTrackingID = utils.uuid()
    Given path '/shipments'
    And header Authorization = 'Bearer '+ bearer
    And segments[0].status = STATUS_COMPLETED
    And segments[1].status = STATUS_COMPLETED
    And segments[2].status = STATUS_COMPLETED
    And request updatedCreateRequest
    When method POST
    Then status 200

    * def response = $
    * def data = response.data
    * match data.shipment_journey.package_journey_segments[0].status == STATUS_COMPLETED
    * match data.shipment_journey.package_journey_segments[1].status == STATUS_COMPLETED
    * match data.shipment_journey.package_journey_segments[2].status == STATUS_COMPLETED

    # remove all segments and retain first segment
    * def bearer = token
    * def shipmentTrackingID = utils.uuid()
    * def filter = function(x, i) { return i < 1 }
    Given path '/shipments'
    And header Authorization = 'Bearer '+ bearer
    And updatedCreateRequest.data.shipment_journey.package_journey_segments = karate.filter(segments, filter)
    And request updatedCreateRequest
    When method POST
    Then status 200

    * def response = $
    * def data = response.data
    * match data.shipment_journey.package_journey_segments == '#[1]'

    # Add 1 segment
    * def bearer = token
    * def shipmentTrackingID = utils.uuid()
    * def filter = function(x, i) { return i < 2 }
    Given path '/shipments'
    And header Authorization = 'Bearer '+ bearer
    And updatedCreateRequest.data.shipment_journey.package_journey_segments = karate.filter(segments, filter)
    And request updatedCreateRequest
    When method POST
    Then status 200

    * def response = $
    * def data = response.data
    * match data.shipment_journey.package_journey_segments == '#[2]'

    # Remove First segment's type and should return error
    * def bearer = token
    * def shipmentTrackingID = utils.uuid()
    Given path '/shipments'
    And header Authorization = 'Bearer '+ bearer
    And segments[0].type = null
    And request updatedCreateRequest
    When method POST
    Then status 400

    * def response = $
    * def data = response.data

    * match data.field_errors[0].field == "data.shipmentJourney.packageJourneySegments[0].type"
    * match data.field_errors[0].message == "must not be null"

    # Remove Transport type and should return an error
    * def bearer = token
    * def shipmentTrackingID = utils.uuid()
    Given path '/shipments'
    And header Authorization = 'Bearer '+ bearer
    And segments[0].type = 'FIRST_MILE'
    And segments[0].transport_type = null
    And request updatedCreateRequest
    When method POST
    Then status 400

    * def response = $
    * print response
    * match response.data.message == 'There is a validation error in your request'
    * match response.data.field_errors[0].field == 'data.shipmentJourney.packageJourneySegments[0].transportType'
    * match response.data.field_errors[0].message == 'must not be null'

    # Add Segment and set required fields
    * def bearer = token
    * def shipmentTrackingID = utils.uuid()
    * def filter = function(x, i) { return i < 3 }
    Given path '/shipments'
    And header Authorization = 'Bearer '+ bearer
    And updatedCreateRequest.data.shipment_journey.package_journey_segments = karate.filter(segments, filter)
    And segments[0].transport_type = 'GROUND'
    And segments[2].cost = '100'
    And segments[2].ref_id = '3'
    And segments[2].partner_id = '00149c06-6d17-45c9-afaa-9ee649701b27'
    And segments[2].currency_id = '0af789f5-1362-40a7-a0a1-3d5d1e018a94'
    And segments[2].instruction = 'this is an instruction'
    And segments[2].pick_up_time = '2022-12-18 16:27:02 +0700'
    And segments[2].drop_off_time = '2022-12-22 16:27:02 +0700'
    And segments[2].master_waybill = '123-12345675'
    And segments[2].transport_type = 'AIR'
    And segments[2].status = STATUS_PLANNED
    And segments[2].sequence = '3'
    And segments[2].type = 'LAST_MILE'
    And segments[2].start_facility.id = segments[2].end_facility.id
    And request updatedCreateRequest
    * print updatedCreateRequest
    When method POST
    Then status 200

    * def response = $
    * print response
    * match response.data.shipment_journey.package_journey_segments == '#[3]'

    # Create 3 Segments
    * def bearer = token
    * def shipmentTrackingID = utils.uuid()
    * def filter = function(x, i) { return i < 3 }
    Given path '/shipments'
    And header Authorization = 'Bearer '+ bearer
    And updatedCreateRequest.data.shipment_journey.package_journey_segments = karate.filter(segments, filter)
    And segments[0].status = STATUS_COMPLETED
    And segments[1].status = STATUS_IN_PROGRESS
    And segments[2].status = STATUS_PLANNED
    And request updatedCreateRequest
    * print updatedCreateRequest
    When method POST
    Then status 200

    * def response = $
    * def data = response.data
    * print data
    * match data.shipment_journey.package_journey_segments == '#[3]'
    * match data.shipment_journey.package_journey_segments[0].status == STATUS_COMPLETED
    * match data.shipment_journey.package_journey_segments[1].status == STATUS_IN_PROGRESS
    * match data.shipment_journey.package_journey_segments[2].status == STATUS_PLANNED
