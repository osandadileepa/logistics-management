Feature: Failing to Create Shipment(s) Feature with Invalid data

  Background:
    * url baseUrl
    * def requestPath = karate.get('singleRQPath', 'classpath:features/shipment/json/single/addSingleShipment_FieldsInvalid.json')
    * def updatedCreateRequest = read(requestPath)
    * print updatedCreateRequest

  @ShipmentCreate @ShipmentCreateWithInvalidData
  @Regression
  Scenario: Create a Shipment
    * def bearer = token
    * def shipmentTrackingID = utils.uuid()
    Given path '/shipments'
    And header Authorization = 'Bearer '+ bearer
    And request updatedCreateRequest
    When method POST
    Then status 400

    * def response = $
    * print response

    * def data = response.data
    * print data

    * match data.code == "VALIDATION_ERROR"
    * match data.message == "There is a validation error in your request"
    * match data.error_reference_id == '#notnull'
    * match data.timestamp == '#notnull'
    * match data.field_errors == '##array'

  @ShipmentCreate @ShipmentCreateWithInvalidData
  @Regression
  Scenario: Create Shipments in Bulk
    * def bulkReqTemp = read(requestPath)
    * def bulkRequest = {}
    * bulkRequest.data = []
    * bulkRequest.data.push(bulkReqTemp.data)
    * def bearer = token
    * def shipmentTrackingID = utils.uuid()
    Given path '/shipments/bulk'
    And header Authorization = 'Bearer '+ bearer
    And request bulkRequest
    When method POST
    Then status 400

    * def response = $
    * def data = response.data
    * print data

    * match data.code == "VALIDATION_ERROR"
    * match data.message == "There is a validation error in your request"
    * match data.error_reference_id == '#notnull'
    * match data.timestamp == '#notnull'
    * match data.field_errors == '##array'
