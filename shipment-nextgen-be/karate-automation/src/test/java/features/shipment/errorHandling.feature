Feature: Error Handling

  Background:
    * url baseUrl
    * def requestPath = karate.get('singleRQPath', 'classpath:features/shipment/json/single/addSingleRQ.json')
    * def createRequest = read(requestPath)
    * createRequest.data.shipment_tracking_id = 'SHP' + utils.uuid().substring(0, 12)
    * createRequest.data.order.id = utils.uuid().substring(0, 10)
    * createRequest.data.order.order_id_label = utils.uuid().substring(0, 10)

  @ErrorHandling @Regression
  Scenario: Error on create shipment with null shipment_tracking_id
    * def bearer = token
    Given path '/shipments'
    And createRequest.data.shipment_tracking_id = null
    And header Authorization = 'Bearer '+ bearer
    And request createRequest
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
    * match data.field_errors[0].field == 'data.shipment_tracking_id'
    * match data.field_errors[0].message == 'must not be blank'
    * match data.field_errors[0].code == 'NotBlank'

  @ErrorHandling @Regression
  Scenario: Error on create shipment with invalid segment status
    * def bearer = token
    Given path '/shipments'
    And createRequest.data.shipment_journey.package_journey_segments[0].status = 'INVALID_ENUM'
    And header Authorization = 'Bearer '+ bearer
    And request createRequest
    When method POST
    Then status 400

    * def response = $
    * def data = response.data
    * print data

    * match data.code == "INVALID_FORMAT"
    * match data.message == "There is an invalid format in your request"
    * match data.error_reference_id == '#notnull'
    * match data.timestamp == '#notnull'
    * match data.field_errors == '##array'
    * match data.field_errors[0].field == 'data.shipment_journey.package_journey_segments[0].status'
    * match data.field_errors[0].message == 'has an invalid format'
    * match data.field_errors[0].code == 'InvalidFormat'
    * match data.field_errors[0].rejected_value == 'INVALID_ENUM'
