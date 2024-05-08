Feature: Rate Calculation

  Background:
    * url baseUrl
    * def rateCardRequest = read('classpath:features/rate-card/json/request.json')
    * path '/rate-cards'
    * request rateCardRequest
    * method POST
    * def rateCard = response.data

    * def calculateRequest = read('json/request.json')
    * calculateRequest.data.rate_card_id = rateCard.id

  @RateCalculation
  Scenario: flat rate calculation
    Given path '/rate-calculation'
    And request calculateRequest
    When method POST
    Then status 200

    * match response.data.result == 0.2500
    * match response.data.rate_card_id == rateCard.id

  @RateCalculation
  Scenario: per weight unit calculation
    * rateCardRequest.data.calculation_type = "PER_WEIGHT_UNIT"
    * path '/rate-cards/' + rateCard.id
    * request rateCardRequest
    * method PUT

    Given path '/rate-calculation'
    And request calculateRequest
    When method POST
    Then status 200

    * match response.data.result == 12.5000
    * match response.data.rate_card_id == rateCard.id

  @RateCalculation
  Scenario: per distance unit calculation
    * rateCardRequest.data.calculation_type = "PER_DISTANCE_UNIT"
    * path '/rate-cards/' + rateCard.id
    * request rateCardRequest
    * method PUT

    Given path '/rate-calculation'
    And request calculateRequest
    When method POST
    Then status 200

    * match response.data.result == 15.0000
    * match response.data.rate_card_id == rateCard.id

  @RateCalculation
  Scenario: good value percentage calculation
    * rateCardRequest.data.calculation_type = "PERCENTAGE_OF_GOOD_VALUE"
    * path '/rate-cards/' + rateCard.id
    * request rateCardRequest
    * method PUT

    Given path '/rate-calculation'
    And request calculateRequest
    When method POST
    Then status 200

    * match response.data.result == 17.5000
    * match response.data.rate_card_id == rateCard.id

  @RateCalculation
  Scenario: below min value
    * rateCardRequest.data.calculation_type = "PERCENTAGE_OF_GOOD_VALUE"
    * rateCardRequest.data.min = 20
    * rateCardRequest.data.max = null
    * path '/rate-cards/' + rateCard.id
    * request rateCardRequest
    * method PUT

    Given path '/rate-calculation'
    And request calculateRequest
    When method POST
    Then status 200

    * match response.data.result == 20
    * match response.data.rate_card_id == rateCard.id

  @RateCalculation
  Scenario: above max value
    * rateCardRequest.data.calculation_type = "PERCENTAGE_OF_GOOD_VALUE"
    * rateCardRequest.data.min = null
    * rateCardRequest.data.max = 15
    * path '/rate-cards/' + rateCard.id
    * request rateCardRequest
    * method PUT

    Given path '/rate-calculation'
    And request calculateRequest
    When method POST
    Then status 200

    * match response.data.result == 15
    * match response.data.rate_card_id == rateCard.id