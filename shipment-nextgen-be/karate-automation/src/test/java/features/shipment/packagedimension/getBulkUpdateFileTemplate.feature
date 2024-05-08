Feature: Retrieve or Download a Bulk Update File Template Feature

  Background:
    * def utils = Java.type('com.quincus.karate.automation.utils.DataUtils')
    * url baseUrl
    * def bearer = token

  @BulkUpdateFileTemplate @Regression @Permission
  Scenario: Retrieve or Download a Bulk Update File Template
    Given url utils.decodeUrl(baseUrl + '/package-dimensions/bulk-update-file-template')
    And header Authorization = 'Bearer ' + bearer
    When method GET
    Then status 200

    * def response = $
    * print response

    * def expectedHeaders = "Shipment ID,Packaging Type,Unit,Height,Width,Length,Weight"
    * def expectedEntry = "sample-shipment-id,sample-packaging-type,METRIC/IMPERIAL,1.00,1.00,1.00,1.00"
    * def startsWithHeader = function(x){ return x.startsWith(expectedHeaders) }
    * def containsEntry = function(x){ return x.contains(expectedEntry) }
    * match true == startsWithHeader(response)
    * match true == containsEntry(response)