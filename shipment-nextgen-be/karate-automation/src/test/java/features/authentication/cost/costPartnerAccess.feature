Feature: Costs > Permission: Partner Access

  Background:
    * url baseUrl
    * def bearer = token

    * def userCredentialsA = 'classpath:session/json/partner_user_001.json'
    * def newSessionA = callonce read('classpath:session/create.feature') {userCredentials : '#(userCredentialsA)'}
    * def newTokenA = newSessionA.response.data.token

    * def userProfileA = callonce read('classpath:session/get-user-profile.feature') {jwtToken : '#(newTokenA)'}
    * print userProfileA.response
    * def partnerIdA = userProfileA.response.partner_id

    * def userCredentialsB = 'classpath:session/json/partner_user_002.json'
    * def newSessionB = callonce read('classpath:session/create.feature') {userCredentials : '#(userCredentialsB)'}
    * def newTokenB = newSessionB.response.data.token

    * def userProfileB = callonce read('classpath:session/get-user-profile.feature') {jwtToken : '#(newTokenB)'}
    * print userProfileB.response
    * def partnerIdB = userProfileB.response.partner_id

    * def userCredentialsA1 = 'classpath:session/json/partner_user_extra_partner_001.json'
    * def newSessionA1 = callonce read('classpath:session/create.feature') {userCredentials : '#(userCredentialsA1)'}
    * def newTokenA1 = newSessionA1.response.data.token

    * def userProfileA1 = callonce read('classpath:session/get-user-profile.feature') {jwtToken : '#(newTokenA1)'}
    * print userProfileA1.response
    * def partnerIdA1 = userProfileA1.response.partner_id

    * def userCredentialsComp1 = 'classpath:session/json/company_user_extra_partner_001.json'
    * def newSessionComp1 = callonce read('classpath:session/create.feature') {userCredentials : '#(userCredentialsComp1)'}
    * def newTokenComp1 = newSessionComp1.response.data.token

    * def userProfileComp1 = callonce read('classpath:session/get-user-profile.feature') {jwtToken : '#(newTokenComp1)'}
    * print userProfileComp1.response
    * def partnerIdComp1 = userProfileComp1.response.partner_id

    * def userCredentialsComp0 = 'classpath:session/json/company_user_no_partner.json'
    * def newSessionComp0 = callonce read('classpath:session/create.feature') {userCredentials : '#(userCredentialsComp0)'}
    * def newTokenComp0 = newSessionComp0.response.data.token

    * def userProfileComp0 = callonce read('classpath:session/get-user-profile.feature') {jwtToken : '#(newTokenComp0)'}
    * print userProfileComp0.response
    * def partnerIdComp0 = userProfileComp0.response.partner_id

    * def jsonPathA = 'classpath:features/shipment/json/single/shipment-partner_001.json'
    * def driverIdA = 'eef82f1f-9a6b-4204-9c7f-40996586b6b8'
    * def createCostA = callonce read('classpath:features/authentication/cost/createCostWithDriver.feature') {jwtToken : '#(newTokenA)', driverId : '#(driverIdA)', partnerId : '#(partnerIdA)', shipmentJson : '#(jsonPathA)'}
    * def costIdA = createCostA.response.data.id
    * def costShipmentIdA = createCostA.shipmentUuid
    * def costShipmentTrackingIdA = createCostA.shipmentTrackingId
    * def updateRequestA = createCostA.response

    * def jsonPathB = 'classpath:features/shipment/json/single/shipment-partner_002.json'
    * def driverIdB = '251efc14-56b4-48dd-a198-8911cccf0ad7'
    * def createCostB = callonce read('classpath:features/authentication/cost/createCostWithDriver.feature') {jwtToken : '#(newTokenB)', driverId : '#(driverIdB)', partnerId : '#(partnerIdB)', shipmentJson : '#(jsonPathB)'}
    * def costIdB = createCostB.response.data.id
    * def costShipmentIdB = createCostB.shipmentUuid
    * def costShipmentTrackingIdB = createCostB.shipmentTrackingId
    * def updateRequestB = createCostB.response

    * def jsonPath0 = 'classpath:features/shipment/json/single/shipment-no-partner.json'
    * def driverId0 = '078ef77b-d592-4670-bb9f-f676fa742f0d'
    * def createCost0 = callonce read('classpath:features/authentication/cost/createCostWithDriver.feature') {jwtToken : '#(newTokenComp0)', driverId : '#(driverId0)', partnerId : '#(partnerIdComp0)', shipmentJson : '#(jsonPath0)'}
    * def costId0 = createCost0.response.data.id
    * def costShipmentId0 = createCost0.shipmentUuid
    * def costShipmentTrackingId0 = createCost0.shipmentTrackingId
    * def updateRequest0 = createCost0.response

    * def getShipmentIds = function(shp){ return shp.id }
    * def costIds = function(cost){ return cost.id }

  @CostPermissions
  Scenario: Happy path: Get cost, user A (partner A) tries to access cost created from partner A
    Given path '/costs/' + costIdA
    And header Authorization = 'Bearer ' + newTokenA
    When method GET
    Then status 200

  @CostPermissions
  Scenario: Unhappy path: Get cost, user A (partner A) tries to access cost created from partner B
    Given path '/costs/' + costIdB
    And header Authorization = 'Bearer '+ newTokenA
    When method GET
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @CostPermissions
  Scenario: Unhappy path: Get cost, user A (partner A) tries to access cost created from company (no partner)
    Given path '/costs/' + costId0
    And header Authorization = 'Bearer '+ newTokenA
    When method GET
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @CostPermissions
  Scenario: Happy path: Get cost, user B (partner B) tries to access cost created from partner B
    Given path '/costs/' + costIdB
    And header Authorization = 'Bearer ' + newTokenB
    When method GET
    Then status 200

  @CostPermissions
  Scenario: Unhappy path: Get cost, user B (partner B) tries to access cost created from partner A
    Given path '/costs/' + costIdA
    And header Authorization = 'Bearer '+ newTokenB
    When method GET
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @CostPermissions
  Scenario: Happy path: Get cost, user A1 (extra partner A) tries to access cost created from partner A
    Given path '/costs/' + costIdA
    And header Authorization = 'Bearer ' + newTokenA1
    When method GET
    Then status 200

  @CostPermissions
  Scenario: Unhappy path: Get cost, user A1 (extra partner A) tries to access cost created from partner B
    Given path '/costs/' + costIdB
    And header Authorization = 'Bearer '+ newTokenA1
    When method GET
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @CostPermissions
  Scenario: Unhappy path: Get cost, user A1 (extra partner A) tries to access cost created from company (no partner)
    Given path '/costs/' + costId0
    And header Authorization = 'Bearer '+ newTokenA1
    When method GET
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @CostPermissions
  Scenario: Happy path: Get cost, company user tries to access cost created from company (no partner)
    Given path '/costs/' + costId0
    And header Authorization = 'Bearer ' + newTokenComp0
    When method GET
    Then status 200

  @CostPermissions
  Scenario: Unhappy path: Get cost, company user tries to access cost created from partner A
    Given path '/costs/' + costIdA
    And header Authorization = 'Bearer '+ newTokenComp0
    When method GET
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @CostPermissions
  Scenario: Happy path: Get cost, company user (extra partner A) tries to access cost created from partner A
    Given path '/costs/' + costIdA
    And header Authorization = 'Bearer ' + newTokenComp1
    When method GET
    Then status 200

  @CostPermissions
  Scenario: Unhappy path: Get cost, company user (extra partner A) tries to access cost created from partner B
    Given path '/costs/' + costIdB
    And header Authorization = 'Bearer '+ newTokenComp1
    When method GET
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @CostPermissions
  Scenario: Happy path: Get cost, company user (extra partner A) tries to access cost created from company (no partner)
    Given path '/costs/' + costId0
    And header Authorization = 'Bearer ' + newTokenComp1
    When method GET
    Then status 200

  @CostPermissions
  Scenario: Happy path: Create cost, user A (partner A) tries to create cost for shipment from partner A
    Given path '/costs'
    And header Authorization = 'Bearer ' + newTokenA
    And request createCostA.createReqBody
    When method POST
    Then status 200

  @CostPermissions
  Scenario: Unhappy path: Create cost, user A (partner A) tries to create cost for shipment from partner B
    Given path '/costs'
    And header Authorization = 'Bearer ' + newTokenA
    And request createCostB.createReqBody
    When method POST
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @CostPermissions
  Scenario: Unhappy path: Create cost, user A (partner A) tries to create cost for shipment from company (no partner)
    Given path '/costs'
    And header Authorization = 'Bearer ' + newTokenA
    And request createCost0.createReqBody
    When method POST
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @CostPermissions
  Scenario: Happy path: Create cost, user B (partner B) tries to create cost for shipment from partner B
    Given path '/costs'
    And header Authorization = 'Bearer ' + newTokenB
    And request createCostB.createReqBody
    When method POST
    Then status 200

  @CostPermissions
  Scenario: Unhappy path: Create cost, user B (partner B) tries to create cost for shipment from partner A
    Given path '/costs'
    And header Authorization = 'Bearer ' + newTokenB
    And request createCostA.createReqBody
    When method POST
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @CostPermissions
  Scenario: Happy path: Create cost, user A1 (extra partner A) tries to create cost for shipment from partner A
    Given path '/costs'
    And header Authorization = 'Bearer ' + newTokenA1
    And request createCostA.createReqBody
    When method POST
    Then status 200

  @CostPermissions
  Scenario: Unhappy path: Create cost, user A1 (extra partner A) tries to create cost for shipment from partner B
    Given path '/costs'
    And header Authorization = 'Bearer ' + newTokenA1
    And request createCostB.createReqBody
    When method POST
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @CostPermissions
  Scenario: Unhappy path: Create cost, user A1 (extra partner A) tries to create cost for shipment from company (no partner)
    Given path '/costs'
    And header Authorization = 'Bearer ' + newTokenA1
    And request createCost0.createReqBody
    When method POST
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @CostPermissions
  Scenario: Happy path: Create cost, company user tries to create cost for shipment from company (no partner)
    Given path '/costs'
    And header Authorization = 'Bearer ' + newTokenComp0
    And request createCost0.createReqBody
    When method POST
    Then status 200

  @CostPermissions
  Scenario: Unhappy path: Create cost, company user tries to create cost for shipment from partner A
    Given path '/costs'
    And header Authorization = 'Bearer ' + newTokenComp0
    And request createCostA.createReqBody
    When method POST
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @CostPermissions
  Scenario: Happy path: Create cost, company user (extra partner A) tries to create cost for shipment from partner A
    Given path '/costs'
    And header Authorization = 'Bearer ' + newTokenComp1
    And request createCostA.createReqBody
    When method POST
    Then status 200

  @CostPermissions
  Scenario: Unhappy path: Create cost, company user (extra partner A) tries to create cost for shipment from partner B
    Given path '/costs'
    And header Authorization = 'Bearer ' + newTokenComp1
    And request createCostB.createReqBody
    When method POST
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @CostPermissions
  Scenario: Happy path: Create cost, company user (extra partner A) tries to create cost for shipment from company (no partner)
    Given path '/costs'
    And header Authorization = 'Bearer ' + newTokenComp1
    And request createCost0.createReqBody
    When method POST
    Then status 200

  @CostPermissions
  Scenario: Happy path: Update cost, user A (partner A) tries to update cost from partner A
    Given path '/costs/' + updateRequestA.data.id
    And header Authorization = 'Bearer ' + newTokenA
    And request updateRequestA
    When method PUT
    Then status 200

  @CostPermissions
  Scenario: Unhappy path: Update cost, user A (partner A) tries to update cost from partner B
    Given path '/costs/' + updateRequestB.data.id
    And header Authorization = 'Bearer ' + newTokenA
    And request updateRequestB
    When method PUT
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @CostPermissions
  Scenario: Unhappy path: Update cost, user A (partner A) tries to update cost from company (no partner)
    Given path '/costs/' + updateRequest0.data.id
    And header Authorization = 'Bearer ' + newTokenA
    And request updateRequest0
    When method PUT
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @CostPermissions
  Scenario: Happy path: Update cost, user B (partner B) tries to update cost from partner B
    Given path '/costs/' + updateRequestB.data.id
    And header Authorization = 'Bearer ' + newTokenB
    And request updateRequestB
    When method PUT
    Then status 200

  @CostPermissions
  Scenario: Unhappy path: Update cost, user B (partner B) tries to update cost from partner A
    Given path '/costs/' + updateRequestA.data.id
    And header Authorization = 'Bearer ' + newTokenB
    And request updateRequestA
    When method PUT
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @CostPermissions
  Scenario: Happy path: Update cost, user A1 (extra partner A) tries to update cost from partner A
    Given path '/costs/' + updateRequestA.data.id
    And header Authorization = 'Bearer ' + newTokenA1
    And request updateRequestA
    When method PUT
    Then status 200

  @CostPermissions
  Scenario: Unhappy path: Update cost, user A1 (extra partner A) tries to update cost from partner B
    Given path '/costs/' + updateRequestB.data.id
    And header Authorization = 'Bearer ' + newTokenA1
    And request updateRequestB
    When method PUT
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @CostPermissions
  Scenario: Unhappy path: Update cost, user A1 (extra partner A) tries to update cost from company (no partner)
    Given path '/costs/' + updateRequest0.data.id
    And header Authorization = 'Bearer ' + newTokenA1
    And request updateRequest0
    When method PUT
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @CostPermissions
  Scenario: Happy path: Update cost, company user tries to update cost from company (no partner)
    Given path '/costs/' + updateRequest0.data.id
    And header Authorization = 'Bearer ' + newTokenComp0
    And request updateRequest0
    When method PUT
    Then status 200

  @CostPermissions
  Scenario: Unhappy path: Update cost, company user tries to update cost from partner A
    Given path '/costs/' + updateRequestA.data.id
    And header Authorization = 'Bearer ' + newTokenComp0
    And request updateRequestA
    When method PUT
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @CostPermissions
  Scenario: Happy path: Update cost, company user (extra partner A) tries to update cost from partner A
    Given path '/costs/' + updateRequestA.data.id
    And header Authorization = 'Bearer ' + newTokenComp1
    And request updateRequestA
    When method PUT
    Then status 200

  @CostPermissions
  Scenario: Unhappy path: Update cost, company user (extra partner A) tries to update cost from partner B
    Given path '/costs/' + updateRequestB.data.id
    And header Authorization = 'Bearer ' + newTokenComp1
    And request updateRequestB
    When method PUT
    Then status 403

    * def response = $
    * match response.message == 'Current user does not meet the required user group to access or modify this record.'
    * match response.code == 'RESOURCE_ACCESS_FORBIDDEN'

  @CostPermissions
  Scenario: Happy path: Update cost, company user (extra partner A) tries to update cost from company (no partner)
    Given path '/costs/' + updateRequest0.data.id
    And header Authorization = 'Bearer ' + newTokenComp1
    And request updateRequest0
    When method PUT
    Then status 200

  @CostPermissions
  Scenario: User A search cost by shipment
    Given url utils.decodeUrl(baseUrl + '/costs/shipments/search?keys=' + costShipmentTrackingIdA + ',' + costShipmentTrackingIdB + ',' + costShipmentTrackingId0)
    And header Authorization = 'Bearer ' + newTokenA
    When method GET
    Then status 200

    * def response = $
    * def data = response.data
    * assert data.total_elements == 1
    * def shipmentIds = karate.map(data.result, getShipmentIds)
    * print shipmentIds
    * match shipmentIds contains costShipmentIdA
    * match shipmentIds !contains costShipmentIdB
    * match shipmentIds !contains costShipmentId0

  @CostPermissions
  Scenario: User B search cost by shipment
    Given url utils.decodeUrl(baseUrl + '/costs/shipments/search?keys=' + costShipmentTrackingIdA + ',' + costShipmentTrackingIdB + ',' + costShipmentTrackingId0)
    And header Authorization = 'Bearer ' + newTokenB
    When method GET
    Then status 200

    * def response = $
    * def data = response.data
    * assert data.total_elements == 1
    * def shipmentIds = karate.map(data.result, getShipmentIds)
    * print shipmentIds
    * match shipmentIds !contains costShipmentIdA
    * match shipmentIds contains costShipmentIdB
    * match shipmentIds !contains costShipmentId0

  @CostPermissions
  Scenario: User A1 (extra partner A) search cost by shipment
    Given url utils.decodeUrl(baseUrl + '/costs/shipments/search?keys=' + costShipmentTrackingIdA + ',' + costShipmentTrackingIdB + ',' + costShipmentTrackingId0)
    And header Authorization = 'Bearer ' + newTokenA1
    When method GET
    Then status 200

    * def response = $
    * def data = response.data
    * assert data.total_elements == 1
    * def shipmentIds = karate.map(data.result, getShipmentIds)
    * print shipmentIds
    * match shipmentIds contains costShipmentIdA
    * match shipmentIds !contains costShipmentIdB
    * match shipmentIds !contains costShipmentId0

  @CostPermissions
  Scenario: Company User search cost by shipment
    Given url utils.decodeUrl(baseUrl + '/costs/shipments/search?keys=' + costShipmentTrackingIdA + ',' + costShipmentTrackingIdB + ',' + costShipmentTrackingId0)
    And header Authorization = 'Bearer ' + newTokenComp0
    When method GET
    Then status 200

    * def response = $
    * def data = response.data
    * assert data.total_elements == 1
    * def shipmentIds = karate.map(data.result, getShipmentIds)
    * print shipmentIds
    * match shipmentIds !contains costShipmentIdA
    * match shipmentIds !contains costShipmentIdB
    * match shipmentIds contains costShipmentId0

  @CostPermissions
  Scenario: Company User (extra partner A) search cost by shipment
    Given url utils.decodeUrl(baseUrl + '/costs/shipments/search?keys=' + costShipmentTrackingIdA + ',' + costShipmentTrackingIdB + ',' + costShipmentTrackingId0)
    And header Authorization = 'Bearer ' + newTokenComp1
    When method GET
    Then status 200

    * def response = $
    * def data = response.data
    * assert data.total_elements >= 1
    * def shipmentIds = karate.map(data.result, getShipmentIds)
    * print shipmentIds
    * match shipmentIds contains costShipmentIdA
    * match shipmentIds !contains costShipmentIdB
    * match shipmentIds contains costShipmentId0

  @CostPermissions
  Scenario: User A lists costs
    Given url utils.decodeUrl(baseUrl + '/costs?per_page=50&page=1&sort_by=modifyTime&sort_dir=desc')
    And header Authorization = 'Bearer ' + newTokenA
    When method GET
    Then status 200

    * def response = $
    * def data = response.data
    * assert data.total_elements >= 1
    * def costIds = karate.map(data.result, costIds)
    * print costIds
    * match costIds contains costIdA
    * match costIds !contains costIdB
    * match costIds !contains costId0

  @CostPermissions
  Scenario: User B lists costs
    Given url utils.decodeUrl(baseUrl + '/costs?per_page=50&page=1&sort_by=modifyTime&sort_dir=desc')
    And header Authorization = 'Bearer ' + newTokenB
    When method GET
    Then status 200

    * def response = $
    * def data = response.data
    * assert data.total_elements >= 1
    * def costIds = karate.map(data.result, costIds)
    * print costIds
    * match costIds !contains costIdA
    * match costIds contains costIdB
    * match costIds !contains costId0

  @CostPermissions
  Scenario: User A1 (extra partner A) lists costs
    Given url utils.decodeUrl(baseUrl + '/costs?per_page=50&page=1&sort_by=modifyTime&sort_dir=desc')
    And header Authorization = 'Bearer ' + newTokenA1
    When method GET
    Then status 200

    * def response = $
    * def data = response.data
    * assert data.total_elements >= 1
    * def costIds = karate.map(data.result, costIds)
    * print costIds
    * match costIds contains costIdA
    * match costIds !contains costIdB
    * match costIds !contains costId0

  @CostPermissions
  Scenario: Company User lists costs
    Given url utils.decodeUrl(baseUrl + '/costs?per_page=50&page=1&sort_by=modifyTime&sort_dir=desc')
    And header Authorization = 'Bearer ' + newTokenComp0
    When method GET
    Then status 200

    * def response = $
    * def data = response.data
    * assert data.total_elements >= 1
    * def costIds = karate.map(data.result, costIds)
    * print costIds
    * match costIds !contains costIdA
    * match costIds !contains costIdB
    * match costIds contains costId0

  @CostPermissions
  Scenario: Company User (extra partner A) lists costs
    Given url utils.decodeUrl(baseUrl + '/costs?per_page=50&page=1&sort_by=modifyTime&sort_dir=desc')
    And header Authorization = 'Bearer ' + newTokenComp1
    When method GET
    Then status 200

    * def response = $
    * def data = response.data
    * assert data.total_elements >= 1
    * def costIds = karate.map(data.result, costIds)
    * print costIds
    * match costIds contains costIdA
    * match costIds !contains costIdB
    * match costIds contains costId0
