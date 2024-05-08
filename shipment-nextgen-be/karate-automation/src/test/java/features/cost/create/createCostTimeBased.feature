Feature: Create Cost for Time-Based

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
    * print shipment

    * def createReqBody = read('classpath:features/cost/json/costRQ.json')
    * createReqBody.data.cost_type.id = costTypesList[0].id
    * createReqBody.data.currency.id = null
    * createReqBody.data.driver_id = null
    * createReqBody.data.partner_id = null
    * createReqBody.data.cost_type.category = 'TIME_BASED'
    * createReqBody.data.cost_type.proof = 'optional'
    * createReqBody.data.cost_type.id = '10d95526-4f18-4497-93b4-e74747f71f13'
    * createReqBody.data.shipments[0].id = shipment.id
    * createReqBody.data.shipments[0].segments[0].segment_id = shipment.shipment_journey.package_journey_segments[0].segment_id
    * createReqBody.data.proof_of_cost = null

  @CostCreate @Regression
  Scenario: Create Cost for time-based
    Given path '/costs/'
    And header Authorization = 'Bearer '+ bearer
    And request createReqBody
    When method POST
    Then status 200

    * def data = response.data
    * match data.cost_amount == '#notnull'
    * match data.remarks == '#notnull'
    * match data.proof_of_cost == '#notpresent'
    * match data.issued_timezone == '#notnull'
    * match data.created_by == '#notnull'
    * match data.created_timezone == '#notnull'
    * match data.modified_by == '#notnull'

    * match data.shipments == '#notnull'
    * match data.shipments[0].shipment_tracking_id == '#notnull'
    * match data.shipments[0].origin == '#notnull'
    * match data.shipments[0].destination == '#notnull'
    * match data.shipments[0].order_id == '#notnull'
    * match data.shipments[0].segments[0].segment_id == '#notnull'
    * match data.shipments[0].segments[0].sequence_no == '#notnull'
    * match data.shipments[0].segments[0].transport_type == '#notnull'

    * match data.cost_type == '#notnull'
    * match data.cost_type.name == '#notnull'
    * match data.cost_type.category == 'TIME_BASED'
    * match data.cost_type.proof == '#notnull'

