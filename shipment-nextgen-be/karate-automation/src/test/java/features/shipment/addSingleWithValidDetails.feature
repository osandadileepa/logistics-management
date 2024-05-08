Feature: Create a Single Shipment Feature

  Background:
    * url baseUrl
    * def requestPath = karate.get('singleRQPath', 'classpath:features/shipment/json/single/shipment-with-valid-details.json')
    * def updatedCreateRequest = read(requestPath)
    * updatedCreateRequest.data.external_order_id = utils.uuid()
    * updatedCreateRequest.data.shipment_tracking_id = 'SHP' + utils.uuid().substring(0, 12)
    * updatedCreateRequest.data.order.id = utils.uuid()
    * updatedCreateRequest.data.order.order_id_label = utils.uuid().substring(0, 10)
    * print updatedCreateRequest

  @ShipmentCreate
  @Regression
  Scenario: Create a Shipment
    * def bearer = token
    * def shipmentTrackingID = utils.uuid()
    Given path '/shipments'
    And header Authorization = 'Bearer '+ bearer
    And request updatedCreateRequest
    When method POST
    Then status 200

    * def response = $
    * print response

    * def data = response.data
    * match data.shipment_tracking_id == '#notnull'
    * match data.shipment_journey.alerts == '#notnull'
    * match data.shipment_reference_id == '##array'
    * match data.status == '#notnull'
    * match data.service_type == '#notnull'
    * match data.service_type.code == '#notnull'
    * match data.service_type.name == '#notnull'
    * match data.sender == '#notnull'
    * match data.consignee == '#notnull'
    * match data.pick_up_location == '#notnull'
    * match data.delivery_location == '#notnull'
    * match data.service_type == '#notnull'
    * match data.user_id == '#notnull'
    * match data.shipment_package == '#notnull'

    * def order = data.order
    * match order == '#notnull'
    * match order.id == '#notnull'
    * match order.order_id_label == '#notnull'

    * def origin = data.origin
    * match origin == '#notnull'
    * match origin contains { country_id: '#string', state_id: '#string', city_id: '#string'}

    * def destination = data.destination
    * match destination == '#notnull'
    * match destination contains { country_id: '#string', state_id: '#string', city_id: '#string'}

    * def package = data.shipment_package
    * match package.type == '#notnull'
    * match package.total_value == '#notnull'
    * match package.dimension.chargeable_weight == '#notnull'
    * match package.dimension.chargeable_weight == '#number'

    * def startFacilityName = updatedCreateRequest.data.shipment_journey.package_journey_segments[0].start_facility.name
    * def startFacilityCode = updatedCreateRequest.data.shipment_journey.package_journey_segments[0].start_facility.code

    * match data.shipment_journey.package_journey_segments[0].start_facility.name == startFacilityName
    * match data.shipment_journey.package_journey_segments[0].start_facility.code == startFacilityCode
