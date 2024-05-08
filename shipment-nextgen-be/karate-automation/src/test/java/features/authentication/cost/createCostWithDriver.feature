Feature: Create Cost with specific driver

  Background:
    * url baseUrl
    * def bearer = karate.get('jwtToken', token)

    * def req1 = callonce read('classpath:features/qportal/listCostTypes.feature')
    * def costTypesList = req1.response.data

    * def req3 = callonce read('classpath:features/qportal/listCurrencies.feature')
    * def currenciesList = req3.response.data

    * def shipmentJson = karate.get('shipmentJson', 'classpath:features/shipment/json/single/addSingleRQ.json')
    * def req4 = call read('classpath:features/shipment/addSingle.feature') {singleRQPath : '#(shipmentJson)'}
    * def shipment = req4.response.data
    * def shipmentUuid = shipment.id
    * def shipmentTrackingId = shipment.shipment_tracking_id

    * def createReqBody = read('classpath:features/cost/json/costRQ.json')
    * createReqBody.data.driver_id = karate.get('driverId', 'eef82f1f-9a6b-4204-9c7f-40996586b6b8')
    * createReqBody.data.partner_id = karate.get('partnerId', null)
    * createReqBody.data.cost_type.id = costTypesList[0].id
    * createReqBody.data.currency.id = currenciesList[0].id
    * createReqBody.data.shipments[0].id = shipmentUuid
    * createReqBody.data.shipments[0].segments[0].segment_id = shipment.shipment_journey.package_journey_segments[0].segment_id

  Scenario: Create Cost with specific driver
    Given path '/costs/'
    And header Authorization = 'Bearer '+ bearer
    And request createReqBody
    When method POST
    Then status 200