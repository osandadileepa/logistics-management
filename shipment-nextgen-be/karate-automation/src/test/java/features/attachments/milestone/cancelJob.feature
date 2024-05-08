Feature: Cancel Job Status

  Background:
    * url baseUrl
    * def bearer = token

    * def bulkUploadMilestone = call read('classpath:features/attachments/milestone/uploadBulk.feature@BulkUploadMilestone')
    * def milestoneJobId = bulkUploadMilestone.data.job_id

  @CancelJobMilestone
  @Regression
  Scenario: Cancel job - milestone
    Given path '/attachments/milestone/cancel-upload/' + milestoneJobId
    And header Authorization = 'Bearer '+ bearer
    When method PUT
    Then status 200

    * def response = $
    * print response