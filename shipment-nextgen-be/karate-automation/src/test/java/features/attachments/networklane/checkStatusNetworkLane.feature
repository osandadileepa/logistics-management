Feature: Upload NetworkLane Csv

  Background:
    * url baseUrl
    * def bearer = token
    * def uploadNetworkLaneResult = call read('classpath:features/attachments/networklane/uploadBulk.feature')
    * print uploadNetworkLaneResult
    * def jobId = uploadNetworkLaneResult.response.data.job_id

  @GetJobStatusNetworkLane
  @Regression
  Scenario: Check Uploaded Csv Status
    Given path "/attachments/network-lane/upload-status/" +jobId
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * print response
    * match response.data.job_id == '#present'
    * match response.data.status == '#present'
    * match response.data.processed_records == '#present'
    * match response.data.successful_records == '#present'
    * match response.data.failed_records == '#present'
    * match response.data.total_records == '#present'
    * match response.data.error_records == '#present'