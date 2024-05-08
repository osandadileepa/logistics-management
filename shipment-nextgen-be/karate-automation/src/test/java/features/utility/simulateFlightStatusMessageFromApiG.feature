Feature: Simulate FlightStatus Message from API-G

  Background:
    * url baseUrl
    * def bearer = token

    * def createShipmentResult = callonce read('classpath:features/shipment/addSingleWithFlight.feature')
    * def shipmentId = createShipmentResult.response.data.id
    * def dummyFlightId = createShipmentResult.response.data.shipment_journey.package_journey_segments[2].flight.flight_id
    * def dummyFlightNumber = createShipmentResult.response.data.shipment_journey.package_journey_segments[2].flight.flight_number

    * print "dummyFlightNumber: " + dummyFlightNumber
    * print "dummyFlightId: " + dummyFlightId

    * def flightStatusMessage = read('classpath:features/utility/json/flightStatusMessage.json')
    * flightStatusMessage.event_payload.flight_number = dummyFlightNumber
    * flightStatusMessage.event_payload.flight_id = dummyFlightId
    * flightStatusMessage.event_payload.flight_status.flight_id = dummyFlightId

  @SimulateFlightStatusMessageFromApiG
  @Regression
  Scenario: Simulate FlightStatus Message from API-G for Flight Departed
    * flightStatusMessage.event_payload.event_name= 'FLIGHT_DEPARTED'
    Given path '/utilities/simulate/apig/receive-flight-event'
    And header Authorization = 'Bearer '+ bearer
    And request flightStatusMessage
    When method POST
    Then status 200

    * utils.sleep(5000)

    Given path '/shipments/' + shipmentId
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * def data = response.data
    * print data
    * match data.milestone.milestone_code == '#present'
    * match data.milestone.milestone_name == '#present'
    * match data.shipment_journey.package_journey_segments[2].flight.flight_statuses[0].departure.scheduled_time == '#present'
    * match data.shipment_journey.package_journey_segments[2].flight.flight_statuses[0].departure.actual_time == '#present'
    * match data.shipment_journey.package_journey_segments[2].flight.flight_statuses[0].arrival.scheduled_time == '#present'
    * match data.shipment_journey.package_journey_segments[2].flight.flight_statuses[0].arrival.actual_time == '#present'
    * match data.shipment_journey.package_journey_segments[2].status == 'IN_PROGRESS'
    * match data.shipment_journey.package_journey_segments[2].transport_type == 'AIR'
    * match data.milestone_events[0].milestone_name == 'Flight Departed'
    * match data.milestone_events[0].user_id == '#notpresent'

  @SimulateFlightStatusMessageFromApiG
  @Regression
  Scenario: Simulate FlightStatus Message from API-G for Flight Landed
    * flightStatusMessage.event_payload.event_name= 'FLIGHT_LANDED'
    Given path '/utilities/simulate/apig/receive-flight-event'
    And header Authorization = 'Bearer '+ bearer
    And request flightStatusMessage
    When method POST
    Then status 200

    * utils.sleep(5000)

    Given path '/shipments/' + shipmentId
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * def data = response.data
    * print data
    * match data.milestone.milestone_code == '#present'
    * match data.milestone.milestone_name == '#present'
    * match data.shipment_journey.package_journey_segments[2].flight.flight_statuses[0].departure.scheduled_time == '#present'
    * match data.shipment_journey.package_journey_segments[2].flight.flight_statuses[0].departure.actual_time == '#present'
    * match data.shipment_journey.package_journey_segments[2].flight.flight_statuses[0].arrival.scheduled_time == '#present'
    * match data.shipment_journey.package_journey_segments[2].flight.flight_statuses[0].arrival.actual_time == '#present'
    * match data.shipment_journey.package_journey_segments[2].status == 'COMPLETED'
    * match data.shipment_journey.package_journey_segments[2].transport_type == 'AIR'
    * match data.milestone_events[0].milestone_name == 'Flight Arrived'
    * match data.milestone_events[0].user_id == '#notpresent'


