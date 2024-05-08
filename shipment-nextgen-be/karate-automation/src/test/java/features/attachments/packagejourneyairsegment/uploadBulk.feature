Feature: Bulk Upload for Package Journey Air Segments

  Background:
    * url baseUrl
    * def bearer = token


  @BulkUploadPackageJourneyAirSegment
  @Regression
  Scenario: Bulk upload a Package Journey Air Segments CSV
    Given path '/attachments/package-journey-air-segment/upload-csv'
    And header Authorization = 'Bearer '+ bearer
    And multipart file file = { read: csv/package-journey-segment-flight-details-pre-populated.csv, filename: 'package-journey-segment-flight-details-pre-populated.csv', contentType: 'text/csv' }
    When method POST
    Then status 200
    * def response = $
    * print response
    * def data = response.data
    * match data.job_id == '#present'