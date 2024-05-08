Feature: Update Package Dimension

  Background:
    * url baseUrl
    * def bearer = token

    * def createShipmentResult = call read('classpath:features/shipment/addSingle.feature')
    * def dimensionId = createShipmentResult.response.data.shipment_package.dimension.id
    * def oldLength = createShipmentResult.response.data.shipment_package.dimension.length
    * def oldWidth = createShipmentResult.response.data.shipment_package.dimension.width
    * def oldHeight = createShipmentResult.response.data.shipment_package.dimension.height
    * def oldGrossWeight = createShipmentResult.response.data.shipment_package.dimension.gross_weight

    * def requestForUpdate = read('classpath:features/shipment/json/updatePackageDimensionRQ.json')
    * requestForUpdate.data.length = 12.123
    * requestForUpdate.data.width = 12.123
    * requestForUpdate.data.height = 12.123
    * requestForUpdate.data.gross_weight = 12.123

    * def shipment_tracking_id = createShipmentResult.data.shipment_tracking_id

    * print requestForUpdate

  @PackageDimensionUpdate @Regression @Permission
  Scenario: Update Package Dimension Individual
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipment_tracking_id + '/package-dimension')
    And header Authorization = 'Bearer '+ bearer
    And request requestForUpdate
    When method PUT
    Then status 200

    * def response = $
    * print response

    * match response.data.previous_package_dimension.length == oldLength
    * match response.data.previous_package_dimension.width == oldWidth
    * match response.data.previous_package_dimension.height == oldHeight
    * match response.data.previous_package_dimension.gross_weight == oldGrossWeight
    * match response.data.new_package_dimension.length == 12.123
    * match response.data.new_package_dimension.width == 12.123
    * match response.data.new_package_dimension.height == 12.123
    * match response.data.new_package_dimension.gross_weight == 12.123
    * match response.data.update_by == '#present'
    * match response.data.updated_at == '#present'
    * match response.data.updated_at == '#present'

  @PackageDimensionUpdate @Regression @Permission
  Scenario: Update Package Dimension Batch
    * requestForUpdate.data.measurement_unit = 'METRIC'
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipment_tracking_id + '/package-dimension')

    And header Authorization = 'Bearer '+ bearer
    And request requestForUpdate
    When method PUT
    Then status 200

    * def response = $
    * print response

    * match response.data.previous_package_dimension.length == oldLength
    * match response.data.previous_package_dimension.width == oldWidth
    * match response.data.previous_package_dimension.height == oldHeight
    * match response.data.previous_package_dimension.gross_weight == oldGrossWeight
    * match response.data.new_package_dimension.length == 12.123
    * match response.data.new_package_dimension.width == 12.123
    * match response.data.new_package_dimension.height == 12.123
    * match response.data.new_package_dimension.gross_weight == 12.123
    * match response.data.new_package_dimension.measurement_unit == requestForUpdate.data.measurement_unit
    * match response.data.update_by == '#present'
    * match response.data.updated_at == '#present'
    * match response.data.updated_at == '#present'

  @PackageDimensionUpdate @Regression @Permission
  Scenario: Update Package Dimension with invalid request
    * requestForUpdate.data.length = -12.123
    * requestForUpdate.data.width = -12.123
    * requestForUpdate.data.height = -12.123
    * requestForUpdate.data.gross_weight = -12.123
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipment_tracking_id + '/package-dimension')

    And header Authorization = 'Bearer '+ bearer
    And request requestForUpdate
    When method PUT
    Then status 400