Feature: List All Service Types For Network Lane Feature

  Background:
    * url baseUrl
    * def bearer = token
    * def uploadNetworkLaneResult = call read('classpath:features/attachments/networklane/uploadBulk.feature')
    * print uploadNetworkLaneResult

  @ListServiceTypes @ListServiceTypesForNetworkLane @Regression
  Scenario: List All Service Types for Network Lane
    * def bearer = token
    Given url baseUrl + '/filter/network-lane/service_types?per_page=10&page=1'
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $

    * match response.data.result != '#[0]'