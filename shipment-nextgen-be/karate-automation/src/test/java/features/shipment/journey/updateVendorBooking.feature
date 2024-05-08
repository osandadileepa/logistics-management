Feature: Receive Update from APIG for Vendor Booking

  Background:
    * url baseUrl
    * def bearer = token

    * def createShipmentsFromOrderResult = call read('classpath:features/shipment/addSingleFromOMPayload.feature')
    * def createdShipmentId1 = createShipmentsFromOrderResult.data[0].id
    * def createdShipmentJourneyId = createShipmentsFromOrderResult.data[0].shipment_journey.journey_id
    * def createdSegmentId1 = createShipmentsFromOrderResult.data[0].shipment_journey.package_journey_segments[0].segment_id
    * def vendorBookingUpdateRequestJson = read('classpath:features/shipment/json/vendorBookingUpdateRequest.json')


  @VendorBookingUpdate @Regression
  Scenario: Update Segment from Vendor Booking Updates with Pending Booking Status
    Given path '/shipment_journeys/'+createdShipmentJourneyId+'/package-journey-segments/'+createdSegmentId1
    And header Authorization = 'Bearer '+ bearer
    * vendorBookingUpdateRequestJson.data.booking_id = utils.uuid()
    * vendorBookingUpdateRequestJson.data.booking_vendor_reference_id = utils.uuid().substring(0, 10)
    * vendorBookingUpdateRequestJson.data.waybill_number = 'WB'+utils.uuid().substring(0, 10)
    And request vendorBookingUpdateRequestJson
    When method PUT
    Then status 200

    * def response = $
    * match response.data == '#present'
    * match response.data.package_journey_segment.segment_id == createdSegmentId1
    * match response.data.package_journey_segment.journey_id == createdShipmentJourneyId

    Given url utils.decodeUrl(baseUrl + '/shipments/'+createdShipmentId1)
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * match response.data.shipment_journey.package_journey_segments[0].booking_status == 'PENDING'
    * match response.data.shipment_journey.package_journey_segments[0].assignment_status == 'Pending'
    * match response.data.shipment_journey.package_journey_segments[0].internal_booking_reference == '#present'
    * match response.data.shipment_journey.package_journey_segments[0].external_booking_reference == '#present'
    * match response.data.shipment_journey.package_journey_segments[0].master_waybill == vendorBookingUpdateRequestJson.data.waybill_number

  @VendorBookingUpdate @Regression
  Scenario: Update Segment from Vendor Booking Updates with Failed Booking Status
    Given path '/shipment_journeys/'+createdShipmentJourneyId+'/package-journey-segments/'+createdSegmentId1
    And header Authorization = 'Bearer '+ bearer
    * vendorBookingUpdateRequestJson.data.booking_status = 'FAILED'
    * vendorBookingUpdateRequestJson.data.booking_id = null
    * vendorBookingUpdateRequestJson.data.booking_vendor_reference_id = null
    * vendorBookingUpdateRequestJson.data.waybill_number = null
    And request vendorBookingUpdateRequestJson
    When method PUT
    Then status 200

    * def response = $
    * match response.data == '#present'
    * match response.data.package_journey_segment.segment_id == createdSegmentId1
    * match response.data.package_journey_segment.journey_id == createdShipmentJourneyId

    Given url utils.decodeUrl(baseUrl + '/shipments/'+createdShipmentId1)
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * match response.data.shipment_journey.alerts == '#present'
    * match response.data.shipment_journey.alerts[0].message == 'System error occurred for vendor assignment [Segment 1]'
    * match response.data.shipment_journey.alerts[0].short_message == 'Assignment: system error'
    * match response.data.shipment_journey.alerts[0].type == 'ERROR'
    * match response.data.shipment_journey.alerts[0].constraint == 'HARD_CONSTRAINT'
    * match response.data.shipment_journey.alerts[0].level == 'CRITICAL'
    * match response.data.shipment_journey.package_journey_segments[0].booking_status == 'FAILED'
    * match response.data.shipment_journey.package_journey_segments[0].assignment_status == 'System Error'
    * match response.data.shipment_journey.package_journey_segments[0].internal_booking_reference == '#notpresent'
    * match response.data.shipment_journey.package_journey_segments[0].external_booking_reference == '#notpresent'
    * match response.data.shipment_journey.package_journey_segments[0].master_waybill == '#notpresent'
    * match response.data.shipment_journey.package_journey_segments[0].alerts == '#present'
    * match response.data.shipment_journey.package_journey_segments[0].alerts[0].message == 'System error occurred for vendor assignment'
    * match response.data.shipment_journey.package_journey_segments[0].alerts[0].short_message == 'Assignment: system error'
    * match response.data.shipment_journey.package_journey_segments[0].alerts[0].type == 'ERROR'
    * match response.data.shipment_journey.package_journey_segments[0].alerts[0].constraint == 'HARD_CONSTRAINT'
    * match response.data.shipment_journey.package_journey_segments[0].alerts[0].level == 'CRITICAL'

  @VendorBookingUpdate @Regression
  Scenario: Update Segment from Vendor Booking Updates with Rejected Booking Status
    Given path '/shipment_journeys/'+createdShipmentJourneyId+'/package-journey-segments/'+createdSegmentId1
    And header Authorization = 'Bearer '+ bearer
    * vendorBookingUpdateRequestJson.data.booking_status = 'REJECTED'
    * vendorBookingUpdateRequestJson.data.booking_id = null
    * vendorBookingUpdateRequestJson.data.rejection_reason = 'Double booking'
    * vendorBookingUpdateRequestJson.data.booking_vendor_reference_id = null
    * vendorBookingUpdateRequestJson.data.waybill_number = null
    And request vendorBookingUpdateRequestJson
    When method PUT
    Then status 200

    * def response = $
    * match response.data == '#present'
    * match response.data.package_journey_segment.segment_id == createdSegmentId1
    * match response.data.package_journey_segment.journey_id == createdShipmentJourneyId

    Given url utils.decodeUrl(baseUrl + '/shipments/'+createdShipmentId1)
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * match response.data.shipment_journey.alerts == '#present'
    * match response.data.shipment_journey.alerts[0].message == 'Vendor rejected assignment [Segment 1]'
    * match response.data.shipment_journey.alerts[0].short_message == 'Assignment rejected'
    * match response.data.shipment_journey.alerts[0].type == 'ERROR'
    * match response.data.shipment_journey.alerts[0].constraint == 'HARD_CONSTRAINT'
    * match response.data.shipment_journey.alerts[0].level == 'CRITICAL'
    * match response.data.shipment_journey.package_journey_segments[0].booking_status == 'REJECTED'
    * match response.data.shipment_journey.package_journey_segments[0].assignment_status == 'Rejected'
    * match response.data.shipment_journey.package_journey_segments[0].internal_booking_reference == '#notpresent'
    * match response.data.shipment_journey.package_journey_segments[0].external_booking_reference == '#notpresent'
    * match response.data.shipment_journey.package_journey_segments[0].master_waybill == '#notpresent'
    * match response.data.shipment_journey.package_journey_segments[0].rejection_reason == 'Double booking'
    * match response.data.shipment_journey.package_journey_segments[0].alerts == '#present'
    * match response.data.shipment_journey.package_journey_segments[0].alerts[0].message == 'Vendor rejected assignment'
    * match response.data.shipment_journey.package_journey_segments[0].alerts[0].short_message == 'Assignment rejected'
    * match response.data.shipment_journey.package_journey_segments[0].alerts[0].type == 'ERROR'
    * match response.data.shipment_journey.package_journey_segments[0].alerts[0].constraint == 'HARD_CONSTRAINT'
    * match response.data.shipment_journey.package_journey_segments[0].alerts[0].level == 'CRITICAL'

  @VendorBookingUpdate @Regression
  Scenario: Update Segment from Vendor Booking Updates with Confirmed Booking Status
    Given path '/shipment_journeys/'+createdShipmentJourneyId+'/package-journey-segments/'+createdSegmentId1
    And header Authorization = 'Bearer '+ bearer
    * vendorBookingUpdateRequestJson.data.booking_status = 'CONFIRMED'
    * vendorBookingUpdateRequestJson.data.booking_id = utils.uuid()
    * vendorBookingUpdateRequestJson.data.booking_vendor_reference_id = utils.uuid().substring(0, 10)
    * vendorBookingUpdateRequestJson.data.waybill_number = 'WB'+utils.uuid().substring(0, 10)
    And request vendorBookingUpdateRequestJson
    When method PUT
    Then status 200

    * def response = $
    * match response.data == '#present'
    * match response.data.package_journey_segment.segment_id == createdSegmentId1
    * match response.data.package_journey_segment.journey_id == createdShipmentJourneyId

    Given url utils.decodeUrl(baseUrl + '/shipments/'+createdShipmentId1)
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * match response.data.milestone.milestone_code == '1111'
    * match response.data.milestone.milestone_name == 'Assignment Scheduled'
    * match response.data.shipment_journey.package_journey_segments[0].booking_status == 'CONFIRMED'
    * match response.data.shipment_journey.package_journey_segments[0].assignment_status == 'Confirmed'
    * match response.data.shipment_journey.package_journey_segments[0].internal_booking_reference == '#present'
    * match response.data.shipment_journey.package_journey_segments[0].external_booking_reference == '#present'
    * match response.data.shipment_journey.package_journey_segments[0].master_waybill == '#present'

  @VendorBookingUpdate @Regression
  Scenario: Update Segment from Vendor Booking Updates with Complete Booking Status
    Given path '/shipment_journeys/'+createdShipmentJourneyId+'/package-journey-segments/'+createdSegmentId1
    And header Authorization = 'Bearer '+ bearer
    * vendorBookingUpdateRequestJson.data.booking_status = 'COMPLETED'
    * vendorBookingUpdateRequestJson.data.booking_id = utils.uuid()
    * vendorBookingUpdateRequestJson.data.booking_vendor_reference_id = utils.uuid().substring(0, 10)
    * vendorBookingUpdateRequestJson.data.waybill_number = 'WB'+utils.uuid().substring(0, 10)
    And request vendorBookingUpdateRequestJson
    When method PUT
    Then status 200

    * def response = $
    * match response.data == '#present'
    * match response.data.package_journey_segment.segment_id == createdSegmentId1
    * match response.data.package_journey_segment.journey_id == createdShipmentJourneyId

    Given url utils.decodeUrl(baseUrl + '/shipments/'+createdShipmentId1)
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * match response.data.shipment_journey.package_journey_segments[0].booking_status == 'COMPLETED'
    * match response.data.shipment_journey.package_journey_segments[0].assignment_status == ''
    * match response.data.shipment_journey.package_journey_segments[0].internal_booking_reference == '#present'
    * match response.data.shipment_journey.package_journey_segments[0].external_booking_reference == '#present'
    * match response.data.shipment_journey.package_journey_segments[0].master_waybill == '#present'

  @VendorBookingUpdate @Regression
  Scenario: Update Segment from Vendor Booking Updates with Cancelled Booking Status
    Given path '/shipment_journeys/'+createdShipmentJourneyId+'/package-journey-segments/'+createdSegmentId1
    And header Authorization = 'Bearer '+ bearer
    * vendorBookingUpdateRequestJson.data.booking_status = 'CANCELLED'
    * vendorBookingUpdateRequestJson.data.booking_id = utils.uuid()
    * vendorBookingUpdateRequestJson.data.booking_vendor_reference_id = utils.uuid().substring(0, 10)
    * vendorBookingUpdateRequestJson.data.waybill_number = 'WB'+utils.uuid().substring(0, 10)
    And request vendorBookingUpdateRequestJson
    When method PUT
    Then status 200

    * def response = $
    * match response.data == '#present'
    * match response.data.package_journey_segment.segment_id == createdSegmentId1
    * match response.data.package_journey_segment.journey_id == createdShipmentJourneyId

    Given url utils.decodeUrl(baseUrl + '/shipments/'+createdShipmentId1)
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * match response.data.milestone.milestone_code == '1110'
    * match response.data.milestone.milestone_name == 'Assignment Cancelled'
    * match response.data.shipment_journey.package_journey_segments[0].booking_status == 'CANCELLED'
    * match response.data.shipment_journey.package_journey_segments[0].assignment_status == '#notpresent'
    * match response.data.shipment_journey.package_journey_segments[0].internal_booking_reference == '#present'
    * match response.data.shipment_journey.package_journey_segments[0].external_booking_reference == '#present'
    * match response.data.shipment_journey.package_journey_segments[0].master_waybill == '#present'