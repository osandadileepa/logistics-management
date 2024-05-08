Feature: Simulate Milestone Message from Dispatch (Parameterized)

  Background:
    * url baseUrl
    * def bearer = karate.get('jwtToken', token)
    * def requestPayload = karate.get('requestPayload', {})

  @SimulateMilestoneMessageFromDispatch
  @Utility
  @ignore
  Scenario: Simulate Milestone Message from Dispatch (Parameterized)
    Given path '/utilities/simulate/dsp/receive-milestone'
    And header Authorization = 'Bearer ' + bearer
    And request requestPayload
    When method POST
    Then status 200
