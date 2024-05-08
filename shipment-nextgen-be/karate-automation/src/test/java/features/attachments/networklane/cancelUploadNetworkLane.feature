Feature: Upload NetworkLane Csv

  Background:
    * url baseUrl
    * def bearer = token
    * def uploadNetworkLaneResult = call read('classpath:features/attachments/networklane/uploadBulk.feature')
    * print uploadNetworkLaneResult
    * def jobId = uploadNetworkLaneResult.response.data.job_id

  @CancelJobNetworkLane
  @Regression
  Scenario: Check Uploaded Csv Status
    Given path "/attachments/network-lane/cancel-upload/" +jobId
    And header Authorization = 'Bearer '+ bearer
    When method PUT
    Then status 200

    * def response = $
    * print response
    * match response.data.job_id == '#present'