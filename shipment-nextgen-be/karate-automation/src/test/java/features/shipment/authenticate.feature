Feature: Authenticate

  Background:
    * url baseUrl

  @ShipmentCreate @Regression
  Scenario: Create a Shipment without Access Token
    Given path '/shipments'
    And request {}
    When method POST
    Then status 401

  @ShipmentCreateBulk @Regression
  Scenario: Create Bulk Shipment without Access Token
    Given path '/shipments/bulk'
    And request {}
    When method POST
    Then status 401

  @ShipmentUpdate @Regression
  Scenario: Update Shipment without Access Token
    Given path '/shipments'
    And request {}
    When method PUT
    Then status 401

  @ShipmentFind @Regression
  Scenario: Retrieve a Shipment without Access Token
    Given path '/shipments/0'
    When method GET
    Then status 401

  @ShipmentFindAll @Regression
  Scenario: Find All Shipments without Access Token
    Given path '/shipments/list'
    And request {}
    When method POST
    Then status 401

  @ShipmentCancel @Regression
  Scenario: Cancel a Shipment without Access Token
    Given path '/shipments/cancel/0'
    When method PATCH
    Then status 401

  @ShipmentExport @Regression
  Scenario: Export Shipments to CSV without Access Token
    Given path '/shipments/export'
    And request {}
    When method POST
    Then status 401