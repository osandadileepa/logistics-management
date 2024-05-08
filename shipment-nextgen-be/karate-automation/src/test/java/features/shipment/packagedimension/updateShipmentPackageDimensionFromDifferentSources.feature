Feature: Update Shipment Package From Different Sources

  Background:
    * url baseUrl
    * def bearer = token

    * def createShipmentsFromOrderResult = callonce read('classpath:features/shipment/addSingleFromOMPayload.feature')
    * def expectedShipmentTrackingId1 = createShipmentsFromOrderResult.data[0].shipment_tracking_id
    * print expectedShipmentTrackingId1
    * def expectedShipmentTrackingId2 = createShipmentsFromOrderResult.data[1].shipment_tracking_id
    * print expectedShipmentTrackingId2

  @UpdateShipmentPackage @Regression
  Scenario: Create Shipments From OM Payload and Verify That Package Source = 'OM'
    * match createShipmentsFromOrderResult.data[0].shipment_tracking_id == expectedShipmentTrackingId1
    * match createShipmentsFromOrderResult.data[0].shipment_package == '#present'
    * match createShipmentsFromOrderResult.data[0].shipment_package.source == 'OM'

    * match createShipmentsFromOrderResult.data[1].shipment_tracking_id == expectedShipmentTrackingId2
    * match createShipmentsFromOrderResult.data[1].shipment_package == '#present'
    * match createShipmentsFromOrderResult.data[1].shipment_package.source == 'OM'

  @UpdateShipmentPackage @ShipmentFind @Regression
  Scenario: Retrieve a Shipment By Shipment Tracking ID and Verify That Package Source = 'OM'
    Given url baseUrl + '/shipments?shipment_tracking_id='+ expectedShipmentTrackingId1
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * match response.data.shipment_tracking_id == expectedShipmentTrackingId1
    * match response.data.shipment_package == '#present'
    * match response.data.shipment_package.source == 'OM'

  @UpdateShipmentPackage @ShipmentFind @Regression
  Scenario: Update a Shipment and Verify That Package Source = 'SHP'
    * def requestForUpdate = read('classpath:features/shipment/json/updatePackageDimensionRQ.json')
    * requestForUpdate.data.length = 12.123

    * print requestForUpdate

    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + expectedShipmentTrackingId1 + '/package-dimension')
    And header Authorization = 'Bearer '+ bearer
    And request requestForUpdate
    When method PUT
    Then status 200

    * def response = $

    * match response.data.previous_package_dimension == '#present'
    * match response.data.new_package_dimension == '#present'
    * match response.data.shipment_package.source == 'SHP'
    * match response.data.update_by == '#present'
    * match response.data.updated_at == '#present'

  @UpdateShipmentPackage @ShipmentFind @Regression
  Scenario: Retrieve a Shipment By Shipment Tracking ID and Verify That Package Source = 'SHP'
    Given url baseUrl + '/shipments?shipment_tracking_id='+ expectedShipmentTrackingId1
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * match response.data.shipment_tracking_id == expectedShipmentTrackingId1
    * match response.data.shipment_package == '#present'
    * match response.data.shipment_package.source == 'SHP'


  @UpdateShipmentPackage @ShipmentFind @Regression
  Scenario: Update Shipments and Verify That Package Source = 'APIG'
    * def requestForUpdate = read('classpath:features/shipment/json/updateShipmentsPackageDimensionRQ.json')
    * requestForUpdate.data[0].shipment_tracking_id = expectedShipmentTrackingId1
    * requestForUpdate.data[0].value_of_goods.length = 12.123
    * requestForUpdate.data[1].shipment_tracking_id = expectedShipmentTrackingId2
    * requestForUpdate.data[1].value_of_goods.length = 13.123

    * print requestForUpdate

    Given url utils.decodeUrl(baseUrl + '/package-dimensions/shipments')
    And header Authorization = 'Bearer '+ bearer
    And request requestForUpdate
    When method POST
    Then status 200

    * def response = $
    * print response

    * match response.data[0].previous_package_dimension == '#present'
    * match response.data[0].new_package_dimension == '#present'
    * match response.data[0].shipment_package.source == 'APIG'
    * match response.data[0].updated_date == '#present'

    * match response.data[1].previous_package_dimension == '#present'
    * match response.data[1].new_package_dimension == '#present'
    * match response.data[1].shipment_package.source == 'APIG'
    * match response.data[1].updated_date == '#present'

  @UpdateShipmentPackage @ShipmentFind @Regression
  Scenario: Retrieve a Shipment By Shipment Tracking ID and Verify That Package Source = 'APIG'
    Given url baseUrl + '/shipments?shipment_tracking_id='+ expectedShipmentTrackingId1
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * match response.data.shipment_tracking_id == expectedShipmentTrackingId1
    * match response.data.shipment_package == '#present'
    * match response.data.shipment_package.source == 'APIG'

  @UpdateShipmentPackage @ShipmentFind @Regression
  Scenario: Retrieve a Shipment By Shipment Tracking ID and Verify That Package Source = 'APIG'
    Given url baseUrl + '/shipments?shipment_tracking_id='+ expectedShipmentTrackingId2
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * match response.data.shipment_tracking_id == expectedShipmentTrackingId2
    * match response.data.shipment_package == '#present'
    * match response.data.shipment_package.source == 'APIG'