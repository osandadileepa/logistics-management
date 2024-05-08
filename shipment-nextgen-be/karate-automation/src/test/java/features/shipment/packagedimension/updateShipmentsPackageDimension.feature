Feature: Update Package Dimension of Multiple Shipments

  Background:
    * url baseUrl
    * def bearer = token

    * def createShipmentResult = call read('classpath:features/shipment/addSingle.feature')
    * def shipmentTrackingId = createShipmentResult.data.shipment_tracking_id
    * def dimensionId = createShipmentResult.response.data.shipment_package.dimension.id
    * def oldLength = createShipmentResult.response.data.shipment_package.dimension.length
    * def oldWidth = createShipmentResult.response.data.shipment_package.dimension.width
    * def oldHeight = createShipmentResult.response.data.shipment_package.dimension.height
    * def oldGrossWeight = createShipmentResult.response.data.shipment_package.dimension.gross_weight

    * def createShipmentResult2 = call read('classpath:features/shipment/addSingle.feature')
    * def shipmentTrackingId2 = createShipmentResult2.data.shipment_tracking_id
    * def dimensionId2 = createShipmentResult2.response.data.shipment_package.dimension.id
    * def oldLength2 = createShipmentResult2.response.data.shipment_package.dimension.length
    * def oldWidth2 = createShipmentResult2.response.data.shipment_package.dimension.width
    * def oldHeight2 = createShipmentResult2.response.data.shipment_package.dimension.height
    * def oldGrossWeight2 = createShipmentResult2.response.data.shipment_package.dimension.gross_weight

    * def requestForUpdate = read('classpath:features/shipment/json/updateShipmentsPackageDimensionRQ.json')
    * requestForUpdate.data[0].shipment_tracking_id = shipmentTrackingId
    * requestForUpdate.data[0].value_of_goods.length = 12.123
    * requestForUpdate.data[0].value_of_goods.width = 12.123
    * requestForUpdate.data[0].value_of_goods.height = 12.123
    * requestForUpdate.data[0].value_of_goods.gross_weight = 12.123
    * requestForUpdate.data[1].shipment_tracking_id = shipmentTrackingId2
    * requestForUpdate.data[1].value_of_goods.length = 13.123
    * requestForUpdate.data[1].value_of_goods.width = 13.123
    * requestForUpdate.data[1].value_of_goods.height = 13.123
    * requestForUpdate.data[1].value_of_goods.gross_weight = 13.123

    * print requestForUpdate

  @ShipmentsPackageDimensionUpdate @Regression
  Scenario: Update Shipments Package Dimension
    Given url utils.decodeUrl(baseUrl + '/package-dimensions/shipments')
    And header Authorization = 'Bearer '+ bearer
    And request requestForUpdate
    When method POST
    Then status 200

    * def response = $
    * print response

    * match response.data[0].previous_package_dimension.length == oldLength
    * match response.data[0].previous_package_dimension.width == oldWidth
    * match response.data[0].previous_package_dimension.height == oldHeight
    * match response.data[0].previous_package_dimension.gross_weight == oldGrossWeight
    * match response.data[0].new_package_dimension.length == 12.123
    * match response.data[0].new_package_dimension.width == 12.123
    * match response.data[0].new_package_dimension.height == 12.123
    * match response.data[0].new_package_dimension.gross_weight == 12.123

    * match response.data[0].shipment_package.source == 'APIG'

    * match response.data[1].previous_package_dimension.length == oldLength2
    * match response.data[1].previous_package_dimension.width == oldWidth2
    * match response.data[1].previous_package_dimension.height == oldHeight2
    * match response.data[1].previous_package_dimension.gross_weight == oldGrossWeight2
    * match response.data[1].new_package_dimension.length == 13.123
    * match response.data[1].new_package_dimension.width == 13.123
    * match response.data[1].new_package_dimension.height == 13.123
    * match response.data[1].new_package_dimension.gross_weight == 13.123

    * match response.data[1].shipment_package.source == 'APIG'

  @ShipmentsPackageDimensionUpdate @Regression
  Scenario: Update Package Dimension with invalid request
    * requestForUpdate.data[0].value_of_goods.length = -12.123
    * requestForUpdate.data[0].value_of_goods.width = -12.123
    * requestForUpdate.data[0].value_of_goods.height = -12.123
    * requestForUpdate.data[0].value_of_goods.gross_weight = -12.123
    * requestForUpdate.data[1].value_of_goods.length = -13.123
    * requestForUpdate.data[1].value_of_goods.width = -13.123
    * requestForUpdate.data[1].value_of_goods.height = -13.123
    * requestForUpdate.data[1].value_of_goods.gross_weight = -13.123

    Given url utils.decodeUrl(baseUrl + '/package-dimensions/shipments')
    And header Authorization = 'Bearer '+ bearer
    And request requestForUpdate
    When method POST
    Then status 400