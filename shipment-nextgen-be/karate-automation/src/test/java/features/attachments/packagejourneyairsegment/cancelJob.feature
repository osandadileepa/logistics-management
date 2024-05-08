Feature: Cancel Job for Package Journey Air Segments

  Background:
    * url baseUrl
    * def bearer = token
    * def bulkUpload = callonce read('classpath:features/attachments/packagejourneyairsegment/uploadBulk.feature@BulkUploadPackageJourneyAirSegment')
    * def jobId = bulkUpload.data.job_id

  @CancelJobPackageJourneyAirSegment
  @Regression
  Scenario: Cancel Package Journey Air Segments
    Given path '/attachments/package-journey-air-segment/cancel-upload/' + jobId
    And header Authorization = 'Bearer '+ bearer
    When method PUT
    Then status 200
    * def response = $
    * print response

  @CancelJobPackageJourneyAirSegment
  @Regression
  Scenario: Cancel Package Journey Air Segments with unknown job id
    Given path '/attachments/package-journey-air-segment/cancel-upload/' + '00000000-0000-0000-0000-000000000000'
    And header Authorization = 'Bearer '+ bearer
    When method PUT
    Then status 404
    * def response = $
    * print response