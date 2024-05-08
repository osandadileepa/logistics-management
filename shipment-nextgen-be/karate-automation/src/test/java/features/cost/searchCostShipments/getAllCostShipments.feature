Feature: Cost Shipment Search With User Location Coverage Specifics

  Background:
    * def utils = Java.type('com.quincus.karate.automation.utils.DataUtils')
    * url baseUrl
    * def bearer = token
    * def requestPath = karate.get('singleRQPath', 'classpath:features/shipment/json/single/addSingleRQ.json')
    * def createShipmentResult = callonce read('classpath:features/shipment/addSingleWithUserPartner001.feature') {singleRQPath : '#(requestPath)'}

    * def searchRequest = read('classpath:features/shipment/json/search/keysTrackingIdsRQ.json')
    * searchRequest.data.keys[0] = createShipmentResult.data.shipment_tracking_id

    * def phCoverageUserCredentials = 'classpath:session/json/philippines_location_coverage_user.json'
    * def phCoverageSession = callonce read('classpath:session/create.feature') {userCredentials : '#(phCoverageUserCredentials)'}
    * def phCoverageToken = phCoverageSession.response.data.token

    * def mmCoverageUserCredentials = 'classpath:session/json/metromanila_location_coverage_user.json'
    * def mmCoverageSession = callonce read('classpath:session/create.feature') {userCredentials : '#(mmCoverageUserCredentials)'}
    * def mmCoverageToken = mmCoverageSession.response.data.token

    * def pasayUserCredentials = 'classpath:session/json/pasay_location_coverage_user.json'
    * def pasaySession = callonce read('classpath:session/create.feature') {userCredentials : '#(pasayUserCredentials)'}
    * def pasayToken = pasaySession.response.data.token

    * def makatiUserCredentials = 'classpath:session/json/makati_location_coverage_user.json'
    * def makatiCoverageSession = callonce read('classpath:session/create.feature') {userCredentials : '#(makatiUserCredentials)'}
    * def makatiCoverageToken = makatiCoverageSession.response.data.token

  @CostShipmentSearch @Regression
  Scenario: Search Cost Shipments with Pasay segment with user with Pasay location coverage should get a data
    * def bearer = pasayToken
    Given url baseUrl + '/costs/shipments/search'
    And header Authorization = 'Bearer '+ bearer
    And request searchRequest
    When method POST
    Then status 200

    * def response = $
    * print response

    * def data = response.data

    * assert data.total_elements == 1
    * assert data.total_pages == 1
    * match data.result == '#[1]'

  @CostShipmentSearch @Regression
  Scenario: Search Cost Shipments with user ph country location coverage user should get the data
    * def bearer = phCoverageToken
    Given url baseUrl + '/costs/shipments/search'
    And header Authorization = 'Bearer '+ bearer
    And request searchRequest
    When method POST
    Then status 200

    * def response = $
    * print response

    * def data = response.data

    * assert data.total_elements == 1
    * assert data.total_pages == 1
    * match data.result == '#[1]'
    * match data.result[0].segments.[0].job_types == '#[2]'
    * match data.result[0].segments.[0].job_types contains 'PICK_UP'
    * match data.result[0].segments.[0].job_types contains 'DROP_OFF'
    * match data.result[0].segments.[1].job_types == '#[2]'
    * match data.result[0].segments.[1].job_types contains 'PICK_UP'
    * match data.result[0].segments.[1].job_types contains 'DROP_OFF'
    * match data.result[0].segments.[2].job_types == '#[2]'
    * match data.result[0].segments.[2].job_types contains 'PICK_UP'
    * match data.result[0].segments.[2].job_types contains 'DROP_OFF'

  @CostShipmentSearch @Regression
  Scenario: Search Cost Shipments with user Metro Manila state location coverage user should get the data
    * def bearer = mmCoverageToken
    Given url baseUrl + '/costs/shipments/search'
    And header Authorization = 'Bearer '+ bearer
    And request searchRequest
    When method POST
    Then status 200

    * def response = $
    * print response

    * def data = response.data

    * assert data.total_elements == 1
    * assert data.total_pages == 1
    * match data.result == '#[1]'

  @CostShipmentSearch @Regression
  Scenario: Unhappy path Search Cost Shipments with user makati only location coverage user should get no data. Shipment segment has no makati
    * def bearer = makatiCoverageToken
    Given url baseUrl + '/costs/shipments/search'
    And header Authorization = 'Bearer '+ bearer
    And request searchRequest
    When method POST
    Then status 200

    * def response = $
    * print response

    * def data = response.data

    * assert data.total_elements == 0
    * assert data.total_pages == 0
    * match data.result[0] == '#notpresent'

  @CostShipmentSearch @Regression
  Scenario: Search Cost Shipments with Pasay segment with user with Pasay location coverage and shipment_tracking_id only should get a data
    * def bearer = pasayToken
    Given url baseUrl + '/costs/shipments/search'
    And header Authorization = 'Bearer '+ bearer
    And request searchRequest
    When method POST
    Then status 200

    * def data = response.data
    * match data.result[0].id == '#notnull'
    * match data.result[0].shipment_tracking_id == '#notnull'
    * match data.result[0].origin == '#notnull'
    * match data.result[0].destination == '#notnull'
    * match data.result[0].order_id == '#notnull'
    * match data.result[0].segments[0].segment_id == '#notnull'
    * match data.result[0].segments[0].sequence_no == '#notnull'
    * match data.result[0].segments[0].transport_type == '#notnull'

  @CostShipmentSearch @Regression
  Scenario: Search Cost Shipments with Pasay segment with user with Pasay location coverage and order_id_label only should get a data
    * def bearer = pasayToken
    Given url baseUrl + '/costs/shipments/search'
    And header Authorization = 'Bearer '+ bearer
    And request searchRequest
    When method POST
    Then status 200

    * def data = response.data
    * match data.result[0].id == '#notnull'
    * match data.result[0].shipment_tracking_id == '#notnull'
    * match data.result[0].origin == '#notnull'
    * match data.result[0].destination == '#notnull'
    * match data.result[0].order_id == '#notnull'
    * match data.result[0].segments[0].segment_id == '#notnull'
    * match data.result[0].segments[0].sequence_no == '#notnull'
    * match data.result[0].segments[0].transport_type == '#notnull'

