Feature: NetworkLane Listing Page Feature

  Background:
    * url baseUrl
    * def bearer = token
    * def uploadNetworkLaneResult = call read('classpath:features/attachments/networklane/uploadBulk.feature')
    * def findAllRequest = read('classpath:features/shipment/json/listNetworkLaneRQ.json')
    Given path '/network-lane/list'
    And header Authorization = 'Bearer '+ bearer
    And request findAllRequest
    When method POST
    Then status 200
    * def response = $
    * def data = response.data
    * assert data.total_elements >= 2
    * def networkLaneId = data.result[0].id

  @Regression
  @NetworkLaneGet
  Scenario: Get Network Lane Successfully
    * def bearer = token
    Given path '/network-lane/' + networkLaneId
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200
    * def response = $
    * print response
    * def data = response.data
    * assert data.id == networkLaneId

