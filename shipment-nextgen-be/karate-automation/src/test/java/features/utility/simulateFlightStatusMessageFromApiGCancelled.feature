Feature: Simulate FlightStatus Cancel Message from API-G

  Background:
    * url baseUrl
    * def bearer = token

    * def createShipmentResult = callonce read('classpath:features/shipment/addSingleWithFlight.feature')
    * def shipmentId = createShipmentResult.response.data.id
    * def dummyFlightId = createShipmentResult.response.data.shipment_journey.package_journey_segments[2].flight.flight_id
    * def dummyFlightNumber = createShipmentResult.response.data.shipment_journey.package_journey_segments[2].flight.flight_number

    * print "dummyFlightNumber: " + dummyFlightNumber
    * print "dummyFlightId: " + dummyFlightId

    * def flightStatusMessage = read('classpath:features/utility/json/flightStatusCancelMessage.json')
    * flightStatusMessage.event_payload.flight_number = dummyFlightNumber
    * flightStatusMessage.event_payload.flight_id = dummyFlightId
    * flightStatusMessage.event_payload.flight_status.flight_id = dummyFlightId

  @SimulateFlightStatusMessageCancelFromApiG
  @Regression
  Scenario: Simulate FlightStatus Message from API-G for Flight Cancelled
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
    * match data.shipment_journey.alerts[1].short_message contains "Flight cancelled"
    * match data.shipment_journey.alerts[1].message contains "The flight has been cancelled. Please review the journey and make any necessary arrangements."
    * match data.shipment_journey.package_journey_segments[2].alerts[1].short_message contains "Flight cancelled"
    * match data.shipment_journey.package_journey_segments[2].alerts[1].message contains "The flight has been cancelled. Please review the journey and make any necessary arrangements."



