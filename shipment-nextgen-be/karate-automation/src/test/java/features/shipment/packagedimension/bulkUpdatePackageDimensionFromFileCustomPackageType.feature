Feature: Bulk Update Package Dimension From File Feature

  Background:
    * url baseUrl
    * def bearer = token

    * def createShipmentResult = callonce read('classpath:features/shipment/addSingle.feature')
    * def created_shipment_tracking_id = createShipmentResult.updatedCreateRequest.data.shipment_tracking_id

    * def fileContents = karate.readAsString('classpath:features/shipment/packagedimension/data/bulk-package-update-template.csv')
    * def editedContents = fileContents.replace('sample-shipment-id', created_shipment_tracking_id)
    * def editedContents = editedContents.replace('sample-packaging-type','')
    # blank package type = custom

  @PackageDimensionBulkUpdate @Regression
  Scenario: Bulk Update Custom Package Dimension
    Given url utils.decodeUrl(baseUrl + '/package-dimensions/bulk-package-dimension-update-import')
    And header Authorization = 'Bearer ' + bearer
    And multipart file file = { value: '#(editedContents)', filename: 'bulk-package-update-template-2.csv', contentType: 'text/csv'  }
    When method POST
    Then status 200

    Given url utils.decodeUrl(baseUrl + '/shipments?shipment_tracking_id=' + created_shipment_tracking_id)
    And header Authorization = 'Bearer ' + bearer
    When method GET
    Then status 200

    * def response = $
    * match response.data.shipment_package.type == 'Custom Type'
    * match response.data.shipment_package.dimension.length == 1.0
    * match response.data.shipment_package.dimension.width == 1.0
    * match response.data.shipment_package.dimension.height == 1.0
    * match response.data.milestone.milestone_code == '1520'
    * match response.data.milestone.milestone_name == 'Update Dims/Weight'
    * match response.data.shipment_package.dimension.custom == true
