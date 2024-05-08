Feature: Create Cost

  Background:
    * url baseUrl
    * def bearer = token

    * def req1 = callonce read('classpath:features/qportal/listCostTypes.feature')
    * def costTypesList = req1.response.data

    * def req2 = callonce read('classpath:features/qportal/listDrivers.feature')
    * def driversList = req2.response.data

    * def req3 = callonce read('classpath:features/qportal/listCurrencies.feature')
    * def currenciesList = req3.response.data

    * def req4 = callonce read('classpath:features/shipment/get.feature')
    * def shipment = req4.response.data

    * def createReqBody = read('classpath:features/cost/json/costRQ.json')
    * createReqBody.data.cost_type.id = costTypesList[0].id
    * createReqBody.data.currency.id = currenciesList[0].id
    * createReqBody.data.driver_id = driversList[0].id
    * createReqBody.data.shipments[0].id = shipment.id
    * createReqBody.data.shipments[0].segments[0].segment_id = shipment.shipment_journey.package_journey_segments[0].segment_id

    * def userCredentials = 'classpath:session/json/no_location_coverage.json'
    * def newSession = callonce read('classpath:session/create.feature') {userCredentials : '#(userCredentials)'}
    * def newToken = newSession.response.data.token

  @CostCreate @Regression
  Scenario: Create Cost
    * def bearer = newToken
    Given path '/costs/'
    And header Authorization = 'Bearer '+ bearer
    And request createReqBody
    When method POST
    Then status 403
