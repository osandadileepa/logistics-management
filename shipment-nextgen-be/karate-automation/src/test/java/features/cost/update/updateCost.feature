Feature: Update Cost

  Background:
    * url baseUrl
    * def bearer = token

    * def createReq = callonce read('classpath:features/cost/create/createCost.feature')
    * def costId = createReq.response.data.id

    * def updateReqBody = createReq.response
    * updateReqBody.data.id = null
    * updateReqBody.data.cost_amount = 4000
    # issued_timezone and created_timezone should be ignored when updating
    * updateReqBody.data.issued_timezone = 'UTC 10:00'
    * updateReqBody.data.created_timezone = 'GMT'
    * updateReqBody.data.remarks = 'Updated remarks'

  @CostGet @Regression
  Scenario: Update Cost
    Given path '/costs/' + costId
    And header Authorization = 'Bearer '+ bearer
    And request updateReqBody
    When method PUT
    Then status 200

    * def data = response.data
    * match data.cost_amount == updateReqBody.data.cost_amount
    * match data.remarks == updateReqBody.data.remarks
    * match data.issued_timezone == 'UTC 14:00'
    * match data.created_timezone == 'UTC'
    * match data.created_by == '#notnull'
    * match data.modified_by == '#notnull'
    * match data.shipments == '#notnull'
    * match data.shipments[0].shipment_tracking_id == '#notnull'
    * match data.shipments[0].origin == '#notnull'
    * match data.shipments[0].destination == '#notnull'
    * match data.shipments[0].order_id == '#notnull'
    * match data.shipments[0].external_order_id == '#notnull'
    * match data.shipments[0].segments[0].segment_id == '#notnull'
    * match data.shipments[0].segments[0].sequence_no == '#notnull'
    * match data.shipments[0].segments[0].transport_type == '#notnull'