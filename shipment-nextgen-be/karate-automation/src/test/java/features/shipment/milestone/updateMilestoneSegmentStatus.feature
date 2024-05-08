Feature: Post Milestone

  Background:
    * url baseUrl
    * def bearer = token
    * def jsonPath = 'classpath:features/shipment/json/single/addSingleRQ.json'
    * def createShipmentResult = callonce read('classpath:features/shipment/addSingle.feature') {singleRQPath : '#(jsonPath)'}
    * def shipmentId = createShipmentResult.data.id
    * def fistSegmentRefId = createShipmentResult.data.shipment_journey.package_journey_segments[0].ref_id
    * def secondSegmentRefId = createShipmentResult.data.shipment_journey.package_journey_segments[1].ref_id
    * def orderNumber = createShipmentResult.data.order.order_id_label
    * def s2sTokenDetails = read('classpath:session/json/s2s_token_and_organization.json')
    * def organizationId = s2sTokenDetails.organization_id
    * def s2sToken = s2sTokenDetails.s2s_token
    * def addMilestoneRequest = read('classpath:features/shipment/json/milestone/addMilestone.json')
    * def IN_PROGRESS_MILESTONE_CODE = '1405'
    * def COMPLETE_MILESTONE_CODE = '1117'
    * def CANCELLED_MILESTONE_CODE = '1501'
    * def FAILED_MILESTONE_CODE = '1502'

  @PostMile @SegmentRegression
  Scenario: Send Milestone with Milestone Code for FAILED_MILESTONE_CODE on First Segment
    Given url utils.decodeUrl(baseUrl + '/milestones')
    And header X-API-AUTHORIZATION = s2sToken
    And header X-ORGANISATION-ID = organizationId
    * addMilestoneRequest.data.segment_id = fistSegmentRefId
    * addMilestoneRequest.data.order_no = orderNumber
    * addMilestoneRequest.data.milestone = FAILED_MILESTONE_CODE
    * addMilestoneRequest.data.milestone_date_and_time = utils.getFormattedDateTimeNow()
    And request addMilestoneRequest
    When method POST
    Then status 200

    Given url utils.decodeUrl(baseUrl + '/shipments/' +  shipmentId)
    And header Authorization = 'Bearer ' + bearer
    When method GET
    Then status 200

    * def response = $
    * match response.data.shipment_journey.package_journey_segments == '#[3]'
    * match response.data.milestone_events == '#[2]'
    * match response.data.shipment_journey.package_journey_segments[1].status == 'PLANNED'

  @PostMile @SegmentRegression
  Scenario: Send Milestone with Milestone Code for IN_PROGRESS_MILESTONE_CODE on First Segment
    Given url utils.decodeUrl(baseUrl + '/milestones')
    And header X-API-AUTHORIZATION = s2sToken
    And header X-ORGANISATION-ID = organizationId
    * addMilestoneRequest.data.segment_id = fistSegmentRefId
    * addMilestoneRequest.data.order_no = orderNumber
    * addMilestoneRequest.data.milestone = IN_PROGRESS_MILESTONE_CODE
    * addMilestoneRequest.data.milestone_date_and_time = utils.getFormattedDateTimeNow()
    And request addMilestoneRequest
    When method POST
    Then status 200

    Given url utils.decodeUrl(baseUrl + '/shipments/' +  shipmentId)
    And header Authorization = 'Bearer ' + bearer
    When method GET
    Then status 200

    * def response = $
    * match response.data.shipment_journey.package_journey_segments == '#[3]'
    * match response.data.milestone_events == '#[3]'
    * match response.data.shipment_journey.package_journey_segments[0].status == 'IN_PROGRESS'

  @PostMile @SegmentRegression
  Scenario: Send Milestone with Milestone Code for COMPLETE_MILESTONE_CODE on First Segment
    Given url utils.decodeUrl(baseUrl + '/milestones')
    And header X-API-AUTHORIZATION = s2sToken
    And header X-ORGANISATION-ID = organizationId
    * addMilestoneRequest.data.segment_id = fistSegmentRefId
    * addMilestoneRequest.data.order_no = orderNumber
    * addMilestoneRequest.data.milestone = COMPLETE_MILESTONE_CODE
    * addMilestoneRequest.data.milestone_date_and_time = utils.getFormattedDateTimeNow()
    And request addMilestoneRequest
    When method POST
    Then status 200

    Given url utils.decodeUrl(baseUrl + '/shipments/' +  shipmentId)
    And header Authorization = 'Bearer ' + bearer
    When method GET
    Then status 200

    * def response = $
    * match response.data.shipment_journey.package_journey_segments == '#[3]'
    * match response.data.milestone_events == '#[4]'
    * match response.data.shipment_journey.package_journey_segments[0].status == 'COMPLETED'

  @PostMile @SegmentRegression
  Scenario: Send Milestone with Milestone Code for CANCELLED_MILESTONE_CODE on Second Segment
    Given url utils.decodeUrl(baseUrl + '/milestones')
    And header X-API-AUTHORIZATION = s2sToken
    And header X-ORGANISATION-ID = organizationId
    * addMilestoneRequest.data.segment_id = secondSegmentRefId
    * addMilestoneRequest.data.order_no = orderNumber
    * addMilestoneRequest.data.milestone = CANCELLED_MILESTONE_CODE
    * addMilestoneRequest.data.milestone_date_and_time = utils.getFormattedDateTimeNow()
    And request addMilestoneRequest
    When method POST
    Then status 200

    Given url utils.decodeUrl(baseUrl + '/shipments/' +  shipmentId)
    And header Authorization = 'Bearer ' + bearer
    When method GET
    Then status 200

    * def response = $
    * match response.data.shipment_journey.package_journey_segments == '#[3]'
    * match response.data.milestone_events == '#[5]'
    * match response.data.shipment_journey.package_journey_segments[1].status == 'CANCELLED'

