Feature: List Connections

  Background:
    * url baseUrl
    * def bearer = token

  @Connections @Regression
  Scenario: List Connections
    Given path '/connections'
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def data = response.data
    * match data.result == '#present'
    * match data.result == '#notnull'
    * match data.size == '#present'
    * match data.size == '#notnull'
    * match data.page == '#present'
    * match data.page == '#notnull'
    * match data.total_pages == '#present'
    * match data.total_pages == '#notnull'
    * match data.total_elements == '#present'
    * match data.total_elements == '#notnull'
    * match data.filter == '#present'
    * match data.filter == '#notnull'
