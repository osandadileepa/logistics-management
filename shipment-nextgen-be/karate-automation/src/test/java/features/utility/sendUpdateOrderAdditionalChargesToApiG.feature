Feature: Send Update Order Additional Charges to API-G

  Background:
    * url baseUrl
    * def bearer = token
    * def createCostResult = callonce read('classpath:features/cost/createCost.feature')
    * def costId = createCostResult.response.data.id

  @UpdateOrderAdditionalCharges
  @Utility
  @ignore
  Scenario: Send Update Order Additional charges to API-G
    Given path '/utilities/simulate/apig/update-order-additional-charges/' + costId
    And header Authorization = 'Bearer '+ bearer
    When method POST
    Then status 200

    * def response = $
    * print response
    * def data = response.data

    * match data[0].message == 'Success'
    * match data[0].status == 'SCC0000'


