Feature: Download Bulk Upload CSV Template for Package Journey Air Segments

  Background:
    * url baseUrl
    * def bearer = token
    * def userCredentials = 'classpath:session/json/karate_user.json'
    * def newSession = callonce read('classpath:session/create.feature') {userCredentials : '#(userCredentials)'}
    * def bearer = newSession.response.data.token

  @DownloadCsvTemplatePackageJourneyAirSegment
  @Regression
  Scenario: Download bulk upload Package Journey Air Segments CSV template
    Given path '/attachments/package-journey-air-segment/download-csv-template'
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200
    * def response = $
    * print response