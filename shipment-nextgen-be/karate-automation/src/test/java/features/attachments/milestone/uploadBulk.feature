Feature: Bulk Upload

  Background:
    * url baseUrl
    * def bearer = token

  @BulkUploadMilestone
  @Regression
  Scenario: Bulk upload a milestone
    Given path '/attachments/milestone/upload-csv'
    And header Authorization = 'Bearer '+ bearer
    And multipart file file = { read: csv/milestone.csv, filename: 'milestone.csv', contentType: 'text/csv' }
    When method POST
    Then status 200

    * def response = $
    * print response

    * def data = response.data
    * match data.job_id == '#present'
    * match data.job_id == '#notnull'