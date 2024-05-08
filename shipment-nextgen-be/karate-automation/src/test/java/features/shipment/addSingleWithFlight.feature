Feature: Create a Single Shipment with segment Feature

  Background:
    * url baseUrl
    * def requestPath = karate.get('singleRQPath', 'classpath:features/shipment/json/single/shipment-with-flight.json')
    * def createRequest = read(requestPath)

    * createRequest.data.shipment_tracking_id = 'SHP' + utils.uuid().substring(0, 12)
    * createRequest.data.order.id = utils.uuid()
    * createRequest.data.order.order_id_label = utils.uuid().substring(0, 10)

    * def dummyFlightId = utils.randomNumber()
    * def dummyFlightNumber = karate.toString(utils.randomNumber())

    * createRequest.data.shipment_journey.package_journey_segments[2].flight.flight_id = dummyFlightId
    * createRequest.data.shipment_journey.package_journey_segments[2].flight.flight_number = dummyFlightNumber
    * createRequest.data.shipment_journey.package_journey_segments[2].flight_number = dummyFlightNumber

  @ShipmentCreate @Regression
  Scenario: Create shipment with one flight segment
    * def bearer = token
    Given path '/shipments'
    And header Authorization = 'Bearer '+ bearer
    And request createRequest
    When method POST
    Then status 200

    * def response = $
    * print response
    * def data = response.data

    * match data.shipment_journey.package_journey_segments == '#[3]'
    * match data.shipment_journey.package_journey_segments[2].flight.flight_id == dummyFlightId
    * match data.shipment_journey.package_journey_segments[2].flight.flight_number == dummyFlightNumber
    * match data.shipment_journey.package_journey_segments[2].flight_number == dummyFlightNumber
