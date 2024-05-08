Feature: Receive Milestone from APIG for Vendor

  Background:
    * url baseUrl
    * def bearer = token


  @VendorMilestoneUpdate @Regression
  Scenario: Receive Milestone Update from APIG With Latest Time
    * def createShipmentsFromOrderResult = call read('classpath:features/shipment/addSingleFromOMPayload.feature')
    * def createdShipmentId = createShipmentsFromOrderResult.data[0].id
    * def createdShipmentJourneyId = createShipmentsFromOrderResult.data[0].shipment_journey.journey_id
    * def createdSegmentId = createShipmentsFromOrderResult.data[0].shipment_journey.package_journey_segments[0].segment_id
    * def vendorUpdateMilestoneRequest = read('classpath:features/shipment/json/vendorMilestoneUpdateRequest.json')
    * vendorUpdateMilestoneRequest.data.milestone = 1405
    * vendorUpdateMilestoneRequest.data.milestone_timestamp = utils.getOffsetDateTimePlusDays(1)
    * vendorUpdateMilestoneRequest.data.segment_id = createdSegmentId
    * vendorUpdateMilestoneRequest.data.shipment_ids[0] = createdShipmentId

    Given path '/shipment_journeys/'+createdShipmentJourneyId+'/milestones'
    And header Authorization = 'Bearer '+ bearer
    And request vendorUpdateMilestoneRequest
    When method POST
    Then status 200

    * def response = $
    * print response
    * match response.data == '#present'

    Given url utils.decodeUrl(baseUrl + '/shipments/'+createdShipmentId)
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * print response
    * match response.data == '#present'
    * match response.data.shipment_journey.package_journey_segments[0].status == 'IN_PROGRESS'


  @VendorMilestoneUpdate @Regression
  Scenario: Receive Milestone Update from APIG With Early Time
    * def createShipmentsFromOrderResult = call read('classpath:features/shipment/addSingleFromOMPayload.feature')
    * def createdShipmentId = createShipmentsFromOrderResult.data[0].id
    * def createdShipmentJourneyId = createShipmentsFromOrderResult.data[0].shipment_journey.journey_id
    * def createdSegmentId = createShipmentsFromOrderResult.data[0].shipment_journey.package_journey_segments[0].segment_id
    * def vendorUpdateMilestoneRequest = read('classpath:features/shipment/json/vendorMilestoneUpdateRequest.json')
    * vendorUpdateMilestoneRequest.data.milestone = 1405
    * vendorUpdateMilestoneRequest.data.milestone_timestamp = utils.getOffsetDateTimeMinusDays(365)
    * vendorUpdateMilestoneRequest.data.segment_id = createdSegmentId
    * vendorUpdateMilestoneRequest.data.shipment_ids[0] = createdShipmentId

    Given path '/shipment_journeys/'+createdShipmentJourneyId+'/milestones'
    And header Authorization = 'Bearer '+ bearer
    And request vendorUpdateMilestoneRequest
    When method POST
    Then status 200

    * def response = $
    * match response.data == '#present'

    Given url utils.decodeUrl(baseUrl + '/shipments/'+createdShipmentId)
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * print response
    * match response.data == '#present'
    * match response.data.shipment_journey.package_journey_segments[0].status == 'PLANNED'


  @VendorMilestoneUpdate @Regression
  Scenario: Receive Milestone Update from APIG With Failed Status
    * def createShipmentsFromOrderResult = call read('classpath:features/shipment/addSingleFromOMPayload.feature')
    * def createdShipmentId = createShipmentsFromOrderResult.data[0].id
    * def createdShipmentJourneyId = createShipmentsFromOrderResult.data[0].shipment_journey.journey_id
    * def createdSegmentId = createShipmentsFromOrderResult.data[0].shipment_journey.package_journey_segments[0].segment_id
    * def vendorUpdateMilestoneRequest = read('classpath:features/shipment/json/vendorMilestoneUpdateRequest.json')
    * vendorUpdateMilestoneRequest.data.milestone = 1502
    * vendorUpdateMilestoneRequest.data.milestone_timestamp = utils.getOffsetDateTimePlusDays(1)
    * vendorUpdateMilestoneRequest.data.segment_id = createdSegmentId
    * vendorUpdateMilestoneRequest.data.shipment_ids[0] = createdShipmentId

    Given path '/shipment_journeys/'+createdShipmentJourneyId+'/milestones'
    And header Authorization = 'Bearer '+ bearer
    And request vendorUpdateMilestoneRequest
    When method POST
    Then status 200

    * def response = $
    * match response.data == '#present'

    Given url utils.decodeUrl(baseUrl + '/shipments/'+createdShipmentId)
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200

    * def response = $
    * print response
    * match response.data == '#present'
    * match response.data.shipment_journey.package_journey_segments[0].status == 'PLANNED'
    * match response.data.shipment_journey.alerts[0].short_message == 'Pick up/delivery failed'


  @VendorMilestoneUpdate @Regression
  Scenario: Receive Milestone Update from APIG With Missing Field
    * def createShipmentsFromOrderResult = call read('classpath:features/shipment/addSingleFromOMPayload.feature')
    * def createdShipmentId = createShipmentsFromOrderResult.data[0].id
    * def createdShipmentJourneyId = createShipmentsFromOrderResult.data[0].shipment_journey.journey_id
    * def createdSegmentId = createShipmentsFromOrderResult.data[0].shipment_journey.package_journey_segments[0].segment_id
    * def vendorUpdateMilestoneRequest = read('classpath:features/shipment/json/vendorMilestoneUpdateRequest.json')
    * vendorUpdateMilestoneRequest.data.milestone = ''
    * vendorUpdateMilestoneRequest.data.milestone_timestamp = utils.getOffsetDateTimePlusDays(1)
    * vendorUpdateMilestoneRequest.data.segment_id = createdSegmentId
    * vendorUpdateMilestoneRequest.data.shipment_ids[0] = createdShipmentId

    Given path '/shipment_journeys/'+createdShipmentJourneyId+'/milestones'
    And header Authorization = 'Bearer '+ bearer
    And request vendorUpdateMilestoneRequest
    When method POST
    Then status 400

    * def response = $
    * print response
    * match response.data == '#present'
    * match response.data.response_message contains 'Missing required field(s)'


  @VendorMilestoneUpdate @Regression
  Scenario: Receive Milestone Update from APIG With Invalid Data Type
    * def createShipmentsFromOrderResult = call read('classpath:features/shipment/addSingleFromOMPayload.feature')
    * def createdShipmentId = createShipmentsFromOrderResult.data[0].id
    * def createdShipmentJourneyId = createShipmentsFromOrderResult.data[0].shipment_journey.journey_id
    * def createdSegmentId = createShipmentsFromOrderResult.data[0].shipment_journey.package_journey_segments[0].segment_id
    * def vendorUpdateMilestoneRequest = read('classpath:features/shipment/json/vendorMilestoneUpdateRequest.json')
    * vendorUpdateMilestoneRequest.data.milestone = 1502
    * vendorUpdateMilestoneRequest.data.milestone_timestamp = 123124
    * vendorUpdateMilestoneRequest.data.segment_id = createdSegmentId
    * vendorUpdateMilestoneRequest.data.shipment_ids[0] = createdShipmentId

    Given path '/shipment_journeys/'+createdShipmentJourneyId+'/milestones'
    And header Authorization = 'Bearer '+ bearer
    And request vendorUpdateMilestoneRequest
    When method POST
    Then status 400

    * def response = $
    * print response
    * match response.data == '#present'
    * match response.data.response_message contains 'Incorrect type for field(s)'


  @VendorMilestoneUpdate @Regression
  Scenario: Receive Milestone Update from APIG With Invalid Journey Id
    * def createShipmentsFromOrderResult = call read('classpath:features/shipment/addSingleFromOMPayload.feature')
    * def createdShipmentId = createShipmentsFromOrderResult.data[0].id
    * def createdShipmentJourneyId = 1234
    * def createdSegmentId = createShipmentsFromOrderResult.data[0].shipment_journey.package_journey_segments[0].segment_id
    * def vendorUpdateMilestoneRequest = read('classpath:features/shipment/json/vendorMilestoneUpdateRequest.json')
    * vendorUpdateMilestoneRequest.data.milestone = 1502
    * vendorUpdateMilestoneRequest.data.milestone_timestamp = utils.getOffsetDateTimePlusDays(1)
    * vendorUpdateMilestoneRequest.data.segment_id = createdSegmentId
    * vendorUpdateMilestoneRequest.data.shipment_ids[0] = createdShipmentId

    Given path '/shipment_journeys/'+createdShipmentJourneyId+'/milestones'
    And header Authorization = 'Bearer '+ bearer
    And request vendorUpdateMilestoneRequest
    When method POST
    Then status 422

    * def response = $
    * print response
    * match response.data == '#present'
    * match response.data.response_message contains 'Milestone not saved because one or more of these fields are not found'


  @VendorMilestoneUpdate @Regression
  Scenario: Receive Milestone Update from APIG With Invalid Shipment Id
    * def createShipmentsFromOrderResult = call read('classpath:features/shipment/addSingleFromOMPayload.feature')
    * def createdShipmentId = 1234
    * def createdShipmentJourneyId = createShipmentsFromOrderResult.data[0].shipment_journey.journey_id
    * def createdSegmentId = createShipmentsFromOrderResult.data[0].shipment_journey.package_journey_segments[0].segment_id
    * def vendorUpdateMilestoneRequest = read('classpath:features/shipment/json/vendorMilestoneUpdateRequest.json')
    * vendorUpdateMilestoneRequest.data.milestone = 1502
    * vendorUpdateMilestoneRequest.data.milestone_timestamp = utils.getOffsetDateTimePlusDays(1)
    * vendorUpdateMilestoneRequest.data.segment_id = createdSegmentId
    * vendorUpdateMilestoneRequest.data.shipment_ids[0] = createdShipmentId

    Given path '/shipment_journeys/'+createdShipmentJourneyId+'/milestones'
    And header Authorization = 'Bearer '+ bearer
    And request vendorUpdateMilestoneRequest
    When method POST
    Then status 422

    * def response = $
    * print response
    * match response.data == '#present'
    * match response.data.response_message contains 'Milestone not saved because one or more of these fields are not found'


  @VendorMilestoneUpdate @Regression
  Scenario: Receive Milestone Update from APIG With Invalid Segment Id
    * def createShipmentsFromOrderResult = call read('classpath:features/shipment/addSingleFromOMPayload.feature')
    * def createdShipmentId = createShipmentsFromOrderResult.data[0].id
    * def createdShipmentJourneyId = createShipmentsFromOrderResult.data[0].shipment_journey.journey_id
    * def createdSegmentId = 1234
    * def vendorUpdateMilestoneRequest = read('classpath:features/shipment/json/vendorMilestoneUpdateRequest.json')
    * vendorUpdateMilestoneRequest.data.milestone = 1502
    * vendorUpdateMilestoneRequest.data.milestone_timestamp = utils.getOffsetDateTimePlusDays(1)
    * vendorUpdateMilestoneRequest.data.segment_id = createdSegmentId
    * vendorUpdateMilestoneRequest.data.shipment_ids[0] = createdShipmentId

    Given path '/shipment_journeys/'+createdShipmentJourneyId+'/milestones'
    And header Authorization = 'Bearer '+ bearer
    And request vendorUpdateMilestoneRequest
    When method POST
    Then status 422

    * def response = $
    * print response
    * match response.data == '#present'
    * match response.data.response_message contains 'Milestone not saved because one or more of these fields are not found'