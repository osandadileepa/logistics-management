Feature: Default Listing Page Feature With User Location Coverage Specifics

  Background:
    * def utils = Java.type('com.quincus.karate.automation.utils.DataUtils')
    * url baseUrl
    * def bearer = token
    * def requestPath = karate.get('singleRQPath', 'classpath:features/shipment/json/single/addSingleRQ.json')
    * def createShipmentResult = callonce read('classpath:features/shipment/addSingle.feature') {singleRQPath : '#(requestPath)'}
    * def shipment_tracking_id = createShipmentResult.data.shipment_tracking_id
    * def findAllRequest = read('classpath:features/shipment/json/listAllFilterKeysRQ.json')
    * findAllRequest.data.keys[0] = shipment_tracking_id

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

  @ShipmentFindAll @Regression
  @ShipmentListingPage
  Scenario: Find Shipments with Pasay segment with user with Pasay location coverage should get a data
    * def bearer = pasayToken
    Given path '/shipments/list'
    And header Authorization = 'Bearer '+ bearer
    And request findAllRequest
    When method POST
    Then status 200

    * def response = $
    * print response

    * def req = findAllRequest.data
    * def data = response.data

    * assert data.total_elements == 1
    * assert data.total_pages == 1
    * match data.result == '#[1]'

  @ShipmentFindAll @Regression
  @ShipmentListingPage
  Scenario: Find Shipments with user ph country location coverage user should get the data
    * def bearer = phCoverageToken
    Given path '/shipments/list'
    And header Authorization = 'Bearer '+ bearer
    And request findAllRequest
    When method POST
    Then status 200

    * def response = $
    * print response

    * def req = findAllRequest.data
    * def data = response.data

    * assert data.total_elements == 1
    * assert data.total_pages == 1
    * match data.result == '#[1]'

  @ShipmentFindAll @Regression
  @ShipmentListingPage
  Scenario: Find Shipments with user Metro Manila state location coverage user should get the data
    * def bearer = mmCoverageToken
    Given path '/shipments/list'
    And header Authorization = 'Bearer '+ bearer
    And request findAllRequest
    When method POST
    Then status 200

    * def response = $
    * print response

    * def req = findAllRequest.data
    * def data = response.data

    * assert data.total_elements == 1
    * assert data.total_pages == 1
    * match data.result == '#[1]'

  @ShipmentFindAll @Regression
  @ShipmentListingPage
  Scenario: Unhappy path Find Shipments with user makati only location coverage user should get no data. Shipment segment has no makati
    * def bearer = makatiCoverageToken
    Given path '/shipments/list'
    And header Authorization = 'Bearer '+ bearer
    And request findAllRequest
    When method POST
    Then status 200

    * def response = $
    * print response

    * def req = findAllRequest.data
    * def data = response.data

    * assert data.total_elements == 0
    * assert data.total_pages == 0
    * match data.result[0] == '#notpresent'
