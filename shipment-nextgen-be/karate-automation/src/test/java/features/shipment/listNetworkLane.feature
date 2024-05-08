Feature: NetworkLane Listing Page Feature

  Background:
    * url baseUrl
    * def bearer = token
    * def findAllRequest = read('classpath:features/shipment/json/listNetworkLaneRQ.json')

  @Ignore
  # TODO: UnIgnore once inserting of networklane is implemented and add network lane before list
  @NetworkLaneFindAll @Regression
  @NetworkLaneListingPage
  Scenario: Find All Network Lane
    * def bearer = token
    Given path '/network-lane/list'
    And header Authorization = 'Bearer '+ bearer
    And request findAllRequest
    When method POST
    Then status 200

    * def response = $
    * print response

    * def req = findAllRequest.data
    * def data = response.data

    * assert data.total_elements >= 1
    * assert data.total_pages >= 1
    * match data.filter.size == req.size
    * match data.current_page == req.page_number
    * match data.result ==  '#present'
