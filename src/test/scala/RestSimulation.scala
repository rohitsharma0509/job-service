/*class RestSimulation extends Simulation {
 
    val httpProtocol = http.baseURL("http://staging.baeldung.com")
 
    val scn = scenario("RestSimulation")
      .exec(http("home").get("/"))
      .pause(23)
      .exec(http("article_1").get("/spring-rest-api-metrics"))
      .pause(39)
      .exec(http("rest_series").get("/rest-with-spring-series"))
      .pause(60)
      .exec(http("rest_category").get("/category/rest/"))
      .pause(26)
      .exec(http("archive").get("/full_archive"))
      .pause(70)
      .exec(http("article_2").get("/spring-data-rest-intro"))
 
    setUp(scn.inject(atOnceUsers(1))).protocols(httpProtocol)
}
*/