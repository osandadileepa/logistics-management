Feature: No Permissions

  Background:
    * url baseUrl
    * def bearer = token

    * def userCredentials = 'classpath:session/json/no_permissions.json'
    * def newSession = callonce read('classpath:session/create.feature') {userCredentials : '#(userCredentials)'}
    * def newToken = newSession.response.data.token

    * def updateShipmentRequest = read('classpath:features/shipment/json/updateRQ.json')
    * updateShipmentRequest.data.id = utils.uuid().substring(0, 10)

    * def exportShipmentRequest = read('classpath:features/shipment/json/exportRQ.json')
    * exportShipmentRequest.data.shipment_tracking_id = utils.uuid()

    * def costCreateUpdateRequest = read('classpath:features/cost/json/costRQ.json')
    * costCreateUpdateRequest.data.cost_type.id = utils.uuid().substring(0, 10)
    * costCreateUpdateRequest.data.currency.id = utils.uuid().substring(0, 10)
    * costCreateUpdateRequest.data.driver_id = utils.uuid().substring(0, 10)
    * costCreateUpdateRequest.data.shipments[0].id = utils.uuid().substring(0, 10)
    * costCreateUpdateRequest.data.shipments[0].segments[0].segment_id = utils.uuid().substring(0, 10)

    * def createShipmentForOperationUpdate = callonce read('classpath:features/shipment/addSingleWithValidDetails.feature')
    * def operationUpdateMilestoneRequest = read('classpath:features/shipment/json/updateShipmentMilestoneAdditionalInfoRQ.json')
    * def shipment_tracking_id = createShipmentForOperationUpdate.data.shipment_tracking_id

    * def operationUpdatePackageDimensionRequest = read('classpath:features/shipment/json/updatePackageDimensionRQ.json')


  @Permissions
  Scenario: Unhappy path: User with no permissions retrieves a shipment
    Given path '/shipments/' + utils.uuid().substring(0, 10)
    And header Authorization = 'Bearer ' + newToken
    When method GET
    Then status 401

  @Permissions
  Scenario: Unhappy path: User with no permissions updates a shipment
    Given path '/shipments'
    And header Authorization = 'Bearer '+ newToken
    And request updateShipmentRequest
    When method PUT
    Then status 401

  @Permissions
  Scenario: Unhappy path: User with no permissions exports a shipment
    Given path '/shipments/export'
    And header Authorization = 'Bearer '+ newToken
    And request exportShipmentRequest
    When method POST
    Then status 401

  @CostPermissions
  Scenario: Happy path: Cost Viewer retrieves a cost
    Given path '/costs/' + utils.uuid().substring(0, 10)
    And header Authorization = 'Bearer ' + newToken
    When method GET
    Then status 401

  @CostPermissions
  Scenario: Happy path: Cost Viewer retrieves cost listing
    Given path '/costs'
    And header Authorization = 'Bearer ' + newToken
    When method GET
    Then status 401

  @CostPermissions
  Scenario: Unhappy path: Cost Viewer creates a cost
    Given path '/costs'
    And header Authorization = 'Bearer '+ newToken
    And request costCreateUpdateRequest
    When method POST
    Then status 401

  @CostPermissions
  Scenario: Unhappy path: Cost Viewer updates a cost
    Given path '/costs/' + utils.uuid().substring(0, 10)
    And header Authorization = 'Bearer '+ newToken
    And request costCreateUpdateRequest
    When method PUT
    Then status 401

  @CostPermissions
  Scenario: Unhappy path: Cost Viewer calls shipment selection
    Given path '/costs/shipments/search'

  @Permissions
  Scenario: Unhappy path: User with no permission operation update - milestone and additional info
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipment_tracking_id + '/milestone-and-additional-info')
    And header Authorization = 'Bearer '+ newToken
    And request operationUpdateMilestoneRequest
    When method PATCH
    Then status 401

  @Permissions
  Scenario: Unhappy path: User with no permission operation update - update package dimension
    Given url utils.decodeUrl(baseUrl + '/shipments/get-by-tracking-id/' + shipment_tracking_id + '/package-dimension')
    And header Authorization = 'Bearer '+ newToken
    And request operationUpdatePackageDimensionRequest
    When method PUT
    Then status 401

  @Permissions
  Scenario: Unhappy path: User with no permission operation update - bulk update package dimension
    Given url utils.decodeUrl(baseUrl + '/package-dimensions/bulk-package-dimension-update-import')
    And header Authorization = 'Bearer ' + newToken
    And multipart file file = {read: 'classpath:features/shipment/packagedimension/data/bulk-package-update-template.csv', filename: 'bulk-package-update-template.csv', contentType: 'text/csv' }
    When method POST
    Then status 401

  @Permissions
  Scenario: Unhappy path: User with no permission operation update - retrieve package dimension
    Given url utils.decodeUrl(baseUrl + '/package-dimensions/bulk-update-file-template')
    And header Authorization = 'Bearer ' + newToken
    When method GET
    Then status 401

  @Permissions
  Scenario: Unhappy path: User with no permission operation update - retrieve or download a bulk update file template
    Given url utils.decodeUrl(baseUrl + '/package-dimensions/bulk-update-file-template')
    And header Authorization = 'Bearer ' + newToken
    When method GET
    Then status 401

  @Permissions
  Scenario: Unhappy path: User with no permission operation update - download a milestone template
    Given url utils.decodeUrl(baseUrl + '/attachments/milestone/download-csv-template')
    And header Authorization = 'Bearer ' + newToken
    When method GET
    Then status 401