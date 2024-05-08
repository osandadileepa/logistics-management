Feature: Search Flight Schedules

  Background:
    * url baseUrl
    * def bearer = token
    * def findFlightSchedulesRequest = read('classpath:features/flight/json/search/flightScheduleRQ.json')
    * def getCurrentDate =
      """
      function() {
        var DateTimeFormatter = Java.type('java.time.format.DateTimeFormatter');
        var dtf = DateTimeFormatter.ofPattern('yyyy-MM-dd');
        var ldt = java.time.LocalDateTime.now();
        return ldt.format(dtf);
      }
      """
    * findFlightSchedulesRequest.data.departure_date = getCurrentDate()
    * print findFlightSchedulesRequest

  @FlightFindSchedules @Regression
  Scenario: Find Flight Schedules
    * def bearer = token
    Given path '/flight/schedules'
    And header Authorization = 'Bearer '+ bearer
    And request findFlightSchedulesRequest
    When method POST
    Then status 200

    * def response = $
    * print response

    * def data = response.data
    * def schedulesCount = karate.sizeOf(data)
    * print schedulesCount

    * assert schedulesCount >= 0