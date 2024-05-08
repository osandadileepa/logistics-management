Feature: Bulk Update Package Dimension From File Feature

  Background:
    * url baseUrl
    * def bearer = token

    * def createShipmentResult = callonce read('classpath:features/shipment/addSingle.feature')

  @PackageDimensionBulkUpdate @Regression @Permission
  Scenario: Bulk Update Package Dimension with Invalid ORG ID (not present in QPortal)
    Given url utils.decodeUrl(baseUrl + '/package-dimensions/bulk-package-dimension-update-import')
    And header Authorization = 'Bearer ' + bearer
    And multipart file file = {read: 'classpath:features/shipment/packagedimension/data/bulk-package-update-template.csv', filename: 'bulk-package-update-template.csv', contentType: 'text/csv' }
    When method POST
    Then status 200

    * def response = $
    * print response

    * match response.data.error_record == '#present'
    * match response.data.error_record[0].failed_reason == 'Shipment ID is invalid. | Packaging Type is invalid.'