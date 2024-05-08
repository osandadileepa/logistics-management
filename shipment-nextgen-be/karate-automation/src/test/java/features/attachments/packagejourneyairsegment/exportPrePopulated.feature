Feature: Export a pre-populated template for Package Journey Air Segments

  Background:
    * url baseUrl
    * def bearer = token
    * def getShipmentDetails = callonce read('classpath:features/shipment/get.feature')
    * def shipmentDetails = getShipmentDetails.response
    * def exportRequest = read('classpath:features/shipment/json/exportRQ.json')
    * exportRequest.data.keys = [shipmentDetails.data.shipment_tracking_id]


  @ExportPrePopulatedPackageJourneyAirSegment
  @Regression
  Scenario: Export a pre-populated Package Journey Air Segments CSV
    Given path '/attachments/package-journey-air-segment/export/pre-populated'
    And header Authorization = 'Bearer '+ bearer
    And request exportRequest
    When method POST
    Then status 200

    * def response = $

    * def shp = shipmentDetails.data
    * def shipment_tracking_id = shp.shipment_tracking_id

    * def expectedHeaders = 'Shipment ID,Unit (Do not edit),Volume Weight (Do not edit),Gross Weight (Do not edit),Airline Code,Flight Number,Departure DateTime,Departure Timezone,Origin Facility,Origin Country,Origin State/Province,Origin City,Airway Bill,Vendor,Lockout DateTime,Lockout Timezone,Arrival Datetime,Arrival Timezone,Recovery DateTime,Recovery Timezone,Segment Note/Instruction Content'
    * def expectedSampleData = 'EXAMPLE1,metric,5,20,SG,1923,2023-09-19T07:00:00,UTC+08:00,SINGAPORE_AIRPORT,SINGAPORE,Central Singapore Community Development Council,SINGAPORE,50712345675,Singapore Airlines,2023-09-19T05:00:00,UTC+08:00,2023-09-19T11:00:00,UTC+08:00,2023-09-19T15:00:00,UTC+08:00,This is sample notes/instruction'
    
    * def startsWithHeader = function(x){ return x.startsWith(expectedHeaders) }
    * def containsEntry = function(x,y){ return x.contains(y) }
    * match true == startsWithHeader(response)
    * match true == containsEntry(response,expectedSampleData)
    * match true == containsEntry(response,shipment_tracking_id)