/*import io.gatling.core.Predef._
 import io.gatling.http.Predef._
 import scala.concurrent.duration._

 class KafkaSimulation extends Simulation{
      val httpProtocol = http.baseUrl("https://apex-my-url-is.in.these.quotes.com");

      val kafkaScenario = scenario("KafkaPerfTest")
      .exec(http("Kafka Request").post("/method/method")
                            .header("Content-Type", "application/json")
                            .body(StringBody(""" 
                              {
                                "logDatetime": "2019-03-18T20:26:38.940Z",
                                "url": "/test",
                                "apiName": "test",
                                "apiVersion": "test",
                                "method": "GET",
                                "status": 200,
                                "vin": "TESTTESTVIN0001",
                                "accessToken": "test",
                                "user": "test",
                                "queryParams": "",
                                "requestHeader": "test",
                                "requestBody": "test",
                                "responseHeader": "test",
                                "responseBody": "test",
                                "responseTime": 200,
                                "traceId": "test",
                                "serviceName": "test",
                                "type": "INBOUND"
                              } 
                              """))
                              .check(status.is(202)));
  setUp(kafkaScenario.inject(
    constantConcurrentUsers(2000) during(4 hours))
    .protocols(httpProtocol)
    .throttle(jumpToRps(500),holdFor(4 hours)));
  }

  */