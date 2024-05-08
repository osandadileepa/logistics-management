Feature: Get Job Status for Package Journey Air Segments

  Background:
    * url baseUrl
    * def bearer = token
    * def bulkUpload = callonce read('classpath:features/attachments/packagejourneyairsegment/uploadBulk.feature@BulkUploadPackageJourneyAirSegment')
    * def jobId = bulkUpload.data.job_id

  @GetJobStatusPackageJourneyAirSegment
  @Regression
  Scenario: Get job status for Package Journey Air Segments
    Given path '/attachments/package-journey-air-segment/upload-status/' + jobId
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200
    * def response = $
    * print response
    * def data = response.data
    * match data.job_id == '#present'
    * match data.job_id == '#notnull'
    * match data.status == '#present'
    * match data.status == '#notnull'
    * match data.processed_records == '#present'
    * match data.processed_records == '#notnull'
    * match data.successful_records == '#present'
    * match data.successful_records == '#notnull'
    * match data.failed_records == '#present'
    * match data.failed_records == '#notnull'
    * match data.total_records == '#present'
    * match data.total_records == '#notnull'
    * match data.error_records == '#present'
    * match data.error_records == '#notnull'