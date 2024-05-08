Feature: Upload NetworkLane Csv

  Background:
    * url baseUrl
    * def bearer = token
    * def csvFile = { read: 'classpath:features/attachments/networklane/csv/network-lane.csv', filename: 'network-lane-success.csv', contentType: 'text/csv' }

  @BulkUploadNetworkLane
  @Regression
  Scenario: Upload NetworkLaneCSV File
    Given path "/attachments/network-lane/upload-csv"
    And multipart file file = csvFile
    And header Authorization = 'Bearer '+ bearer
    When method POST
    Then status 200

    * def response = $
    * print response
    * match response.data.job_id == '#present'