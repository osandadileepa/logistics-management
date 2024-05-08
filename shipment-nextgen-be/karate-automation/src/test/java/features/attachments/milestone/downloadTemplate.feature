Feature: Download Bulk Upload CSV Template

  Background:
    * url baseUrl

    * def userCredentials = 'classpath:session/json/karate_user.json'
    * def newSession = callonce read('classpath:session/create.feature') {userCredentials : '#(userCredentials)'}
    * def bearer = newSession.response.data.token

  @DownloadCsvTemplateMilestone
  @Regression
  Scenario: Download bulk upload milestone CSV template
    Given path '/attachments/milestone/download-csv-template'
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * print response