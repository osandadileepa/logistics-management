Feature: Create a Single Shipment with segment Feature

  Background:
    * url baseUrl
    * def requestPath = karate.get('singleRQPath', 'classpath:features/shipment/json/single/shipment-with-alerts.json')
    * def createRequest = read(requestPath)

    * createRequest.data.shipment_tracking_id = 'SHP' + utils.uuid().substring(0, 12)
    * createRequest.data.order.id = utils.uuid().substring(0, 10)
    * createRequest.data.order.order_id_label = utils.uuid().substring(0, 10)

  @ShipmentCreate @Regression @ShipmentWithAlert
  Scenario: Create shipment with alerts
    * def bearer = token
    Given path '/shipments'
    And header Authorization = 'Bearer '+ bearer
    And request createRequest
    When method POST
    Then status 200

    * def response = $
    * print response
    * def data = response.data

    * match data.shipment_journey.alerts == '#[1]'
    * match data.shipment_journey.alerts[0].short_message == 'Blank mandatory field'
    * match data.shipment_journey.alerts[0].message == 'Mandatory field(s) is blank. [Segment 1][Segment 2][Segment 3]'
    * match data.shipment_journey.alerts[0].type == 'ERROR'
    * match data.shipment_journey.alerts[0].constraint == 'HARD_CONSTRAINT'
    * match data.shipment_journey.alerts[0].dismissed == false

    * match data.shipment_journey.package_journey_segments == '#[3]'
    * match data.shipment_journey.package_journey_segments[0].alerts[0].short_message == 'Blank mandatory field'
    * match data.shipment_journey.package_journey_segments[0].alerts[0].message == 'Mandatory field(s) is blank.'
    * match data.shipment_journey.package_journey_segments[0].alerts[0].type == 'ERROR'
    * match data.shipment_journey.package_journey_segments[0].alerts[0].dismissed == false
    * match data.shipment_journey.package_journey_segments[1].alerts[0].short_message == 'Blank mandatory field'
    * match data.shipment_journey.package_journey_segments[1].alerts[0].message == 'Mandatory field(s) is blank.'
    * match data.shipment_journey.package_journey_segments[1].alerts[0].type == 'ERROR'
    * match data.shipment_journey.package_journey_segments[1].alerts[0].dismissed == false
    * match data.shipment_journey.package_journey_segments[2].alerts[0].short_message == 'Blank mandatory field'
    * match data.shipment_journey.package_journey_segments[2].alerts[0].message == 'Mandatory field(s) is blank.'
    * match data.shipment_journey.package_journey_segments[2].alerts[0].type == 'ERROR'
    * match data.shipment_journey.package_journey_segments[2].alerts[0].dismissed == false