Feature: Generate Read File Pre-signed URL Feature

  Background:
    * url baseUrl

  @S3Integration
  @Regression
  @GenerateReadPreSignedUrl
  Scenario: Generate Read File Pre-signed URL - JPG File From shipment_attachments Directory
    * def bearer = token
    * def fileName = "karate-test-sample-file.jpg"
    * def directory = "shipment_attachments"
    Given url baseUrl + '/files/read-pre-signed-link?file_name=' + fileName + '&directory=' + directory
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * print response

    * match response.data[0].url_type == 'PRE_SIGNED_READ'
    * match response.data[0].url != '#null'

  @S3Integration
  @Regression
  @GenerateReadPreSignedUrl
  Scenario: Generate Read File Pre-signed URL - PDF File From shipment_attachments Directory
    * def bearer = token
    * def fileName = "karate-pdf-file.pdf"
    * def directory = "shipment_attachments"
    Given url baseUrl + '/files/read-pre-signed-link?file_name=' + fileName + '&directory=' + directory
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * print response

    * match response.data[0].url_type == 'PRE_SIGNED_READ'
    * match response.data[0].url != '#null'

  @S3Integration
  @Regression
  @GenerateReadPreSignedUrl
  Scenario: Generate Read File Pre-signed URL - PNG File From cost_attachments Directory
    * def bearer = token
    * def fileName = "karate-png-file.png"
    * def directory = "cost_attachments"
    Given url baseUrl + '/files/read-pre-signed-link?file_name=' + fileName + '&directory=' + directory
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * print response

    * match response.data[0].url_type == 'PRE_SIGNED_READ'
    * match response.data[0].url != '#null'

  @S3Integration
  @Regression
  @GenerateReadPreSignedUrl
  Scenario: Generate Read File Pre-signed URL - JPG File From cost_attachments Directory
    * def bearer = token
    * def fileName = "karate-png-file.png"
    * def directory = "cost_attachments"
    Given url baseUrl + '/files/read-pre-signed-link?file_name=' + fileName + '&directory=' + directory
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * print response

    * match response.data[0].url_type == 'PRE_SIGNED_READ'
    * match response.data[0].url != '#null'