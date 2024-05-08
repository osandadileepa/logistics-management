Feature: Delete Existing File Feature

  Background:
    * url baseUrl
    * def fileName = "karate-test-sample2-file.jpg"
    * def directory = "shipment_attachments"

  @S3Integration
  @Regression
  @DeleteExistingFile
  Scenario: Generate Read File Pre-signed URL
    * def bearer = token
    Given url baseUrl + '/files?file_name=' + fileName + '&directory=' + directory
    And header Authorization = 'Bearer '+ bearer
    When method DELETE
    Then status 204
