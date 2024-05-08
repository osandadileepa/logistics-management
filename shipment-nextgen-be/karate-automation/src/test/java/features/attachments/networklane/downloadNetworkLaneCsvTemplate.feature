Feature: Download NetworkLane Csv Template

  Background:
    * url baseUrl
    * def bearer = token

  @DownloadCsvTemplateNetworkLane
  @Regression
  Scenario: Download NetworkLane CSV template
    Given path "/attachments/network-lane/download-csv-template"
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * print responseHeaders
    * match responseHeaders.Content-Disposition[0] contains "attachment"
    * match responseHeaders.Content-Disposition[0] contains "network-lane-template.csv"
    * match responseHeaders.Content-Type[0] contains "text/csv"