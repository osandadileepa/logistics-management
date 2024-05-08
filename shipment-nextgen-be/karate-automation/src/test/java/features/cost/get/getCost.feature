Feature: Get Cost

  Background:
    * url baseUrl
    * def bearer = token

    * def createReq = callonce read('classpath:features/cost/create/createCost.feature')
    * def costId = createReq.response.data.id

  @CostGet @Regression
  Scenario: Get Cost
    Given path '/costs/' + costId
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def data = response.data
    * match data.cost_amount == '#notnull'
    * match data.remarks == '#notnull'
    * match data.organization_id == '#notnull'
    * match data.proof_of_cost == '#notnull'
    * match data.shipments == '#notnull'
    * match data.shipments[0].shipment_tracking_id == '#notnull'
    * match data.shipments[0].origin == '#notnull'
    * match data.shipments[0].destination == '#notnull'
    * match data.shipments[0].order_id == '#notnull'
    * match data.shipments[0].segments[0].segment_id == '#notnull'
    * match data.shipments[0].segments[0].sequence_no == '#notnull'
    * match data.shipments[0].segments[0].transport_type == '#notnull'
    * match data.driver_id == '#notnull'
    * match data.driver_name == '#notnull'
    * match data.cost_type == '#notnull'
    * match data.currency == '#notnull'


