/**
 * Copyright 2011-2017 GatlingCorp (http://gatling.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package restfulbooker

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class RestfulBookerSimulation extends Simulation {

  object Browse {
    val browse = repeat(100) { 
      exec(http("get_all_bookings")
      .get("/booking"))
      .pause(300 milliseconds)
      .exec(http("get_by_name")
      .get("/booking?lastname=Smith"))
      .pause(250 milliseconds)
    .exec(http("get_by_id")
      .get("/booking/3")) 
      .pause(425 milliseconds)
    }
  }

  object Book {
    val feeder = csv("rb_names.csv").random

    val book = repeat(20){
      feed(feeder)
      .exec(
        http("create_booking") // Here's an example of a POST request
        .post("/booking")
			  .body(StringBody(
          """{
              "firstname" : "${firstName}",
              "lastname" : "${lastName}",
              "totalprice" : 111,
              "depositpaid" : true,
              "additionalneeds" : "Breakfast",
              "bookingdates" : {
                "checkin" : "2013-02-23",
                "checkout" : "2014-10-23"
              }
            }""")).asJSON
      )
      .pause(5)
    }
  }

  val httpConf = http
    .baseURL("http://localhost:3001") // Here is the root for all relative URLs
    .acceptHeader("*/*") // Here are the common headers
    .contentTypeHeader("application/json")

  // val scn = scenario("Create and Find Bookings") // A scenario is a chain of requests and pauses
  //   .exec(http("get_all_bookings")
  //     .get("/booking"))
  //   .exec(http("get_by_name")
  //     .get("/booking?lastname=Smith"))
  //   .exec(http("get_by_id")
  //     .get("/booking/3"))
  //   .exec(http("create_booking") // Here's an example of a POST request
  //     .post("/booking")
	// 		.body(RawFileBody("RestfulBooker_create_request.txt")))

  val lookers = scenario("Lookers").exec(Browse.browse)
  val bookers = scenario("Bookers").exec(Book.book)

  setUp(
    lookers.inject(rampUsers(100) over (30 seconds)),
    bookers.inject(rampUsers(30) over (10 seconds))
  ).protocols(httpConf)

  // setUp(scn.inject(atOnceUsers(1)).protocols(httpConf))
}
