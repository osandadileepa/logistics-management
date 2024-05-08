Feature: Create a Single Shipment Feature

  Background:
    * url baseUrl
    * def requestPath = karate.get('singleRQPath', 'classpath:features/shipment/json/single/addSingleRQ.json')
    * def updatedCreateRequest = read(requestPath)
    * updatedCreateRequest.data.shipment_tracking_id = 'SHP' + utils.uuid().substring(0, 12)
    * updatedCreateRequest.data.order.id = utils.uuid()
    * updatedCreateRequest.data.order.order_id_label = utils.uuid().substring(0, 10)
    * print updatedCreateRequest

    * def userCredentials = 'classpath:session/json/partner_user_001.json'
    * def newSession = callonce read('classpath:session/create.feature') {userCredentials : '#(userCredentials)'}
    * def partner001_token = newSession.response.data.token

  @ShipmentCreate
  @Regression
  Scenario: Create a Shipment with Partner001 user
    * def bearer = partner001_token
    * def shipmentTrackingID = utils.uuid()
    Given path '/shipments'
    And header Authorization = 'Bearer '+ bearer
    And request updatedCreateRequest
    When method POST
    Then status 200

    * def response = $
    * print response

    * def data = response.data
    * match data.id == '#present'
    * match data.id == '#notnull'
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
    * match data.internal_order_id == '#notnull'
    * match data.external_order_id == '#notnull'
    * match data.customer_order_id == '#notnull'

    * def order = data.order
    * match order == '#notnull'
    * match order.id == '#notnull'
    * match order.order_id_label == '#notnull'

    * def origin = data.origin
    * match origin == '#notnull'

    * def destination = data.destination
    * match destination == '#notnull'

    * def package = data.shipment_package
    * match package.type == '#notnull'
    * match package.value == '#notnull'
    * match package.dimension.chargeable_weight == '#notnull'
    * match package.dimension.chargeable_weight == '#number'
    * match package.commodities[0].name == '#notnull'
    * match package.commodities[0].description == '#notnull'
    * match package.commodities[0].code == '#notnull'
    * match package.commodities[0].hs_code == '#notnull'
    * match package.commodities[0].note == '#notnull'
    * match package.commodities[0].packaging_type == '#notnull'

    * def startFacilityName = updatedCreateRequest.data.shipment_journey.package_journey_segments[0].start_facility.name
    * def startFacilityCode = updatedCreateRequest.data.shipment_journey.package_journey_segments[0].start_facility.code

    * match data.shipment_journey.package_journey_segments[0].start_facility.name == startFacilityName
    * match data.shipment_journey.package_journey_segments[0].start_facility.code == startFacilityCode
