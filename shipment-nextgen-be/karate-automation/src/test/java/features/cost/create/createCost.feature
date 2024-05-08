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

    * def req5 = callonce read('classpath:features/shipment/get.feature')
    * def shipment2 = req5.response.data

    * def createReqBody = read('classpath:features/cost/json/costRQ.json')
    * createReqBody.data.cost_type.id = costTypesList[0].id
    * createReqBody.data.cost_amount = 100000000000000.00
    * createReqBody.data.currency.id = currenciesList[0].id
    * createReqBody.data.driver_id = driversList[0].id
    * createReqBody.data.shipments[0].id = shipment.id
    * createReqBody.data.shipments[0].segments[0].segment_id = shipment.shipment_journey.package_journey_segments[0].segment_id
    * createReqBody.data.shipments[0].segments[0].job_types= ['PICK_UP', 'DROP_OFF']

  @CostCreate @Regression
  Scenario: Create Cost Unhappy path cost amount over 15 digits
    Given path '/costs/'
    And header Authorization = 'Bearer '+ bearer
    * createReqBody.data.cost_type.id = costTypesList[0].id
    * createReqBody.data.cost_amount = 1000000000000000.00
    * createReqBody.data.currency.id = currenciesList[0].id
    * createReqBody.data.driver_id = driversList[0].id
    * createReqBody.data.shipments[0].id = shipment2.id
    * createReqBody.data.shipments[0].segments[0].segment_id = shipment2.shipment_journey.package_journey_segments[0].segment_id
    * createReqBody.data.shipments[0].segments[0].job_types= ['PICK_UP', 'DROP_OFF']
    And request createReqBody
    When method POST
    Then status 400

  @CostCreate @Regression
  Scenario: Create Cost
    Given path '/costs/'
    And header Authorization = 'Bearer '+ bearer
    And request createReqBody
    When method POST
    Then status 200

    * def data = response.data
    * match data.cost_amount == '#notnull'
    * match data.remarks == '#notnull'
    * match data.proof_of_cost == '#notnull'
    * match data.issued_timezone == '#notnull'
    * match data.created_by == '#notnull'
    * match data.created_timezone == 'UTC'
    * match data.modified_by == '#notnull'
    * match data.source == '#notnull'

    * match data.shipments == '#notnull'
    * match data.shipments[0].shipment_tracking_id == '#notnull'
    * match data.shipments[0].origin == '#notnull'
    * match data.shipments[0].destination == '#notnull'
    * match data.shipments[0].order_id == '#notnull'
    * match data.shipments[0].segments[0].segment_id == '#notnull'
    * match data.shipments[0].segments[0].sequence_no == '#notnull'
    * match data.shipments[0].segments[0].transport_type == '#notnull'
    * match data.shipments[0].segments[0].job_types == '#notnull'
    * match data.shipments[0].segments[0].job_types == '#[2]'
    * match data.shipments[0].segments[0].job_types contains 'PICK_UP'
    * match data.shipments[0].segments[0].job_types contains 'DROP_OFF'
    * match data.shipments[0].external_order_id == '#notnull'


    * match data.driver_id == driversList[0].id
    * match data.driver_name == '#notnull'

    * match data.cost_type == '#notnull'
    * match data.cost_type.id == costTypesList[0].id
    * match data.cost_type.name == costTypesList[0].name
    * match data.cost_type.category == costTypesList[0].category
    * match data.cost_type.proof == costTypesList[0].proof

    * match data.currency == '#notnull'
    * match data.currency.id == currenciesList[0].id
    * match data.currency.name == currenciesList[0].name
    * match data.currency.code == currenciesList[0].code
    * match data.currency.symbol == currenciesList[0].symbol