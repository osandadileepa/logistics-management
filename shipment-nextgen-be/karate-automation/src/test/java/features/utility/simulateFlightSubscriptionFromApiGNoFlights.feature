Feature: Simulate Flight Subscription Message No Flights Found from API-G

  Background:
    * url baseUrl
    * def bearer = token

    * def createShipmentResult = callonce read('classpath:features/shipment/addSingleWithFlight.feature')
    * def shipmentId = createShipmentResult.response.data.id
    * def dummyFlightId = createShipmentResult.response.data.shipment_journey.package_journey_segments[2].flight.flight_id
    * def dummyFlightNumber = createShipmentResult.response.data.shipment_journey.package_journey_segments[2].flight.flight_number

    * print "dummyFlightNumber: " + dummyFlightNumber
    * print "dummyFlightId: " + dummyFlightId

    * def flightStatusMessage = read('classpath:features/utility/json/flightSubscribeMessageNoFlight.json')
    * flightStatusMessage.event_payload.flight_number = dummyFlightNumber

  @SimulateFlightSubscriptionMessageNoFlightsFoundFromApiG
  @Regression
  Scenario: Simulate Flight Subscription Message from API-G for No flight found
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
    * match data.shipment_journey.alerts == '#present'
    * match data.shipment_journey.alerts[1].short_message contains "Flight untrackable"
    * match data.shipment_journey.alerts[1].message contains "The flight cannot be tracked as it is not found on Flightstats."
    * match data.shipment_journey.package_journey_segments[2].alerts == '#present'
    * match data.shipment_journey.package_journey_segments[2].alerts == '#[2]'
    * match data.shipment_journey.package_journey_segments[2].alerts[1].short_message contains "Flight untrackable"
    * match data.shipment_journey.package_journey_segments[2].alerts[1].message contains "The flight cannot be tracked as it is not found on Flightstats."