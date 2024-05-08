Feature: Export to CSV Feature

  Background:
    * url baseUrl
    * def bearer = token
    * def getShipmentDetails = callonce read('classpath:features/shipment/get.feature')
    * def shipmentDetails = getShipmentDetails.response
    * def exportRequest = read('classpath:features/shipment/json/exportRQ.json')
    * exportRequest.data.keys = [shipmentDetails.data.shipment_tracking_id]
    * print exportRequest

  @ShipmentExport @Regression
  Scenario: Export Shipments to CSV
    * configure connectTimeout = 60000
    Given path '/shipments/export'
    And header Authorization = 'Bearer '+ bearer
    And request exportRequest
    When method POST
    Then status 200

    * def response = $
    * print response

    * def shp = shipmentDetails.data
    * def segment = shp.shipment_journey.package_journey_segments[0]
    * def shipment_tracking_id = shp.shipment_tracking_id
    * def origin_country_name = shp.origin.country_name
    * def origin_state_name = shp.origin.state_name
    * def origin_city_name = shp.origin.city_name
    * def destination_country_name = shp.destination.country_name
    * def destination_state_name = shp.destination.state_name
    * def destination_city_name = shp.destination.city_name
    * def ref_id = segment.ref_id
    * def transport_type = segment.transport_type
    * def partner_name = karate.get('segment.partner.name', '')
    * def vehicle_info = karate.get('segment.vehicle_info', '')
    * def flight_number = karate.get('segment.flight_number', '')
    * def airline = karate.get('segment.airline', '')
    * def airline_code = karate.get('segment.airline_code', '')
    * def mwb = karate.get('segment.master_waybill', '')
    * def start_facility = karate.get('segment.start_facility.name', '')
    * def end_facility = karate.get('segment.end_facility.name', '')
    * def pickup_instruction = karate.get('shp.instructions[0].value', '')
    * def delivery_instruction = karate.get('shp.instructions[1].value', '')
    * def segment_instruction = karate.get('segment.instruction', '')
    * def duration = karate.get('segment.duration', '')
    * def duration_unit = karate.get('segment.duration_unit', '')
    * def pick_up_time = karate.get('segment.pick_up_time', '')
    * def drop_off_time = karate.get('segment.drop_off_time', '')
    * def lock_out_time = karate.get('segment.lock_out_time', '')
    * def departure_time = karate.get('segment.departure_time', '')
    * def arrival_time = karate.get('segment.arrival_time', '')
    * def recovery_time = karate.get('segment.recovery_time', '')
    * def calculated_mileage = karate.get('segment.calculated_mileage', '')
    * def calculated_mileage_unit = karate.get('segment.calculated_mileage_unit', '')
    * def milestone_name = karate.get('shp.milestone.milestone_name', '')
    * def service_type = karate.get('shp.service_type.name', '')
    * def customer_name = karate.get('shp.customer.name', '')
    * def eta_status = karate.get('shp.eta_status', '')

    * def expectedHeaders = "Shipment Tracking ID,Origin Country,Origin State,Origin City,Destination Country,Destination State,Destination City,Sequence No.,Transport Category,Partner Name,Vehicle Info,Flight Number,Airline,Airline Code,Master Waybill,Pick Up Facility,Drop Off Facility,Pick Up Instruction,Drop Off Instruction,Segment Instruction,Duration,Duration Unit,Pick Up Time,Drop Off Time,Lockout Time,Departure Time,Arrival Time,Recovery Time,Calculated Mileage,Calculated Mileage Unit,Latest Milestone,Service Type,Customer,Status"
    * def expectedEntry1 = shipment_tracking_id + "," + origin_country_name + "," + origin_state_name + "," + origin_city_name + "," + destination_country_name + "," + destination_state_name + "," + destination_city_name + "," + ref_id + "," + transport_type + "," + partner_name + "," + vehicle_info + "," + flight_number + "," + airline + "," + airline_code + "," + mwb + "," + start_facility + "," + end_facility + "," + pickup_instruction + "," + delivery_instruction
    * def expectedEntry2 = segment_instruction + "," + duration + "," + duration_unit + "," + pick_up_time + "," + drop_off_time + "," + lock_out_time + "," + departure_time + "," + arrival_time + "," + recovery_time + "," + calculated_mileage + "," + calculated_mileage_unit + "," + milestone_name + "," + service_type + "," + customer_name + "," + eta_status
    * print expectedEntry2
    * def startsWithHeader = function(x){ return x.startsWith(expectedHeaders) }
    * def containsEntry = function(x,y){ return x.contains(y) }
    * match true == startsWithHeader(response)
    * match true == containsEntry(response,expectedEntry1)
    * match true == containsEntry(response,expectedEntry2)
    * match true == containsEntry(response,pickup_instruction)
    * match true == containsEntry(response,delivery_instruction)