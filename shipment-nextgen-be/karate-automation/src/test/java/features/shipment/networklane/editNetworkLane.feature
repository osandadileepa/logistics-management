Feature: NetworkLane Listing Page Feature

  Background:
    * url baseUrl
    * def bearer = token
    * def uploadNetworkLaneResult = callonce read('classpath:features/attachments/networklane/uploadBulk.feature')
    * def findAllRequest = read('classpath:features/shipment/json/listNetworkLaneRQ.json')
    Given path '/network-lane/list'
    And header Authorization = 'Bearer '+ bearer
    And request findAllRequest
    When method POST
    Then status 200
    * def response = $
    * def data = response.data
    * assert data.total_elements >= 2
    * def editableNetworkLaneId = data.result[0].id
    * def newOriginFacility = data.result[1].network_lane_segments[0].start_facility
    * def newDestinationFacility = data.result[1].network_lane_segments[0].end_facility
    * def newNetworkLaneSegment = data.result[1].network_lane_segments[data.result[1].network_lane_segments.length - 1]

  @Regression
  @NetworkLaneEdit
  @NetworkLaneEditPage
  Scenario: Edit Network Lane Successfully
    * def bearer = token
    * def payload = read('classpath:features/shipment/json/editNetworkLaneRQ.json')
    * payload.data.id = editableNetworkLaneId
    * payload.data.network_lane_segments[0].start_facility = newOriginFacility
    * payload.data.network_lane_segments[payload.data.network_lane_segments.length - 1].end_facility = newDestinationFacility
    Given path '/network-lane'
    And header Authorization = 'Bearer '+ bearer
    And request payload
    When method PUT
    Then status 200
    * def response = $
    * print response

    * def data = response.data

    * assert data.id == editableNetworkLaneId
    * assert data.origin_facility.id == newOriginFacility.id
    * assert data.destination_facility.id == newDestinationFacility.id
    * assert data.network_lane_segments.length == payload.data.network_lane_segments.length

  @Regression
  @NetworkLaneEdit
  @NetworkLaneEditPage
  Scenario: Edit Network Lane Successfully - Add Another Segment
    * def bearer = token
    * def payload = read('classpath:features/shipment/json/editNetworkLaneRQ.json')
    * payload.data.id = editableNetworkLaneId
    * payload.data.network_lane_segments[0].start_facility = newOriginFacility
    * payload.data.network_lane_segments[payload.data.network_lane_segments.length - 1].end_facility = newDestinationFacility
    * newNetworkLaneSegment.start_facility = payload.data.network_lane_segments[payload.data.network_lane_segments.length - 1].end_facility
    * payload.data.network_lane_segments[payload.data.network_lane_segments.length] = newNetworkLaneSegment
    Given path '/network-lane'
    And header Authorization = 'Bearer '+ bearer
    And request payload
    When method PUT
    Then status 200
    * def response = $
    * print response
    * def data = response.data
    * assert data.id == editableNetworkLaneId
    * assert data.origin_facility.id == newOriginFacility.id
    * assert data.destination_facility.id == newNetworkLaneSegment.end_facility.id
    * assert data.network_lane_segments.length == payload.data.network_lane_segments.length

    Given path '/network-lane/' + editableNetworkLaneId
    And header Authorization = 'Bearer '+ bearer
    When method GET
    Then status 200
    * def response = $
    * def data = response.data
    * assert data.id == editableNetworkLaneId
    * print data.network_lane_segments.length
    * assert data.network_lane_segments.length == payload.data.network_lane_segments.length


  @Regression
  @NetworkLaneEdit
  @NetworkLaneEditPage
  Scenario: Edit Network Lane - Not Found
    * def bearer = token
    * def payload = read('classpath:features/shipment/json/editNetworkLaneRQ.json')
    * def networkLaneIdThatDoesNotExist = "3934c105-f805-4469-adcd-0c78f388f060"
    * payload.data.id = networkLaneIdThatDoesNotExist
    * payload.data.network_lane_segments[0].start_facility = newOriginFacility
    * payload.data.network_lane_segments[payload.data.network_lane_segments.length - 1].end_facility = newDestinationFacility
    Given path '/network-lane'
    And header Authorization = 'Bearer '+ bearer
    And request payload
    When method PUT
    Then status 404
    * def response = $
    * print response

    * assert response.message == "NetworkLane Id " + networkLaneIdThatDoesNotExist + " not found."
    * assert response.code == "INVALID_NETWORK_LANE_ERROR"

  @Regression
  @NetworkLaneEdit
  @NetworkLaneEditPage
  Scenario: Edit Network Lane Validation Error - Identical First and Last Node Facility
    * def bearer = token
    * def payload = read('classpath:features/shipment/json/editNetworkLaneRQ.json')
    * payload.data.id = editableNetworkLaneId
    * payload.data.network_lane_segments[0].start_facility = newOriginFacility
    * payload.data.network_lane_segments[payload.data.network_lane_segments.length - 1].end_facility = newOriginFacility
    Given path '/network-lane'
    And header Authorization = 'Bearer '+ bearer
    And request payload
    When method PUT
    Then status 400
    * def response = $
    * print response

    * def data = response.data

    * assert data.code == "VALIDATION_ERROR"
    * assert data.message == "Invalid Network Lane"
    * assert data.field_errors[0].message == 'networklane.origin_facility and destination_facility Identical first & last network nodes.'

  @Regression
  @NetworkLaneEdit
  @NetworkLaneEditPage
  Scenario: Edit Network Lane Validation Error - Nodes Not Connected
    * def bearer = token
    * def payload = read('classpath:features/shipment/json/editNetworkLaneRQ.json')
    * payload.data.id = editableNetworkLaneId
    * payload.data.network_lane_segments[0].start_facility = newOriginFacility
    * payload.data.network_lane_segments[payload.data.network_lane_segments.length - 1].end_facility = newDestinationFacility
    # Change start & end IDs to simulate disconnected nodes
    * payload.data.network_lane_segments[0].end_facility.id = "4601e04c-f5b7-47f8-89a3-dcc36ec858f6"
    * payload.data.network_lane_segments[1].start_facility.id = "9ff46f38-9fc5-40a6-a561-6fd27c24177d"
    Given path '/network-lane'
    And header Authorization = 'Bearer '+ bearer
    And request payload
    When method PUT
    Then status 400
    * def response = $
    * print response

    * def data = response.data

    * assert data.code == "VALIDATION_ERROR"
    * assert data.message == "Invalid Network Lane"
    * assert data.field_errors[0].message == 'networklane.network_lane_segments[0].end_facility and network_lane_segments[1].start_facility Nodes not connected.'

  @Regression
  @NetworkLaneEdit
  @NetworkLaneEditPage
  Scenario: Edit Network Lane Validation Error - Missing Location
    * def bearer = token
    * def payload = read('classpath:features/shipment/json/editNetworkLaneRQ.json')
    * payload.data.id = editableNetworkLaneId
    * payload.data.network_lane_segments[0].start_facility = newOriginFacility
    * payload.data.network_lane_segments[payload.data.network_lane_segments.length - 1].end_facility = newDestinationFacility
    # Change location to null to simulate missing location from segments
    * payload.data.network_lane_segments[0].end_facility.location = null
    Given path '/network-lane'
    And header Authorization = 'Bearer '+ bearer
    And request payload
    When method PUT
    Then status 400
    * def response = $
    * print response

    * def data = response.data

    * assert data.code == "VALIDATION_ERROR"
    * assert data.message == "Invalid Network Lane"
    * assert data.field_errors[0].message == 'networklane.network_lane_segments[0].end_facility.location Location is missing.'




