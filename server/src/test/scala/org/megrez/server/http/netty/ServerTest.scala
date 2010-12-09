package org.megrez.server.http.netty

import org.scalatest.{Spec, BeforeAndAfterEach}
import org.scalatest.matchers.ShouldMatchers
import org.megrez.server.HttpClientSupport
import resources.Person
import java.net.{URL, URI}
import java.io.File

class ServerTest extends Spec with ShouldMatchers with BeforeAndAfterEach with HttpClientSupport {
  describe("Server") {
    it("should declare resource package") {
      import Route._

      server = new Server(
        resources("org.megrez.server.http.netty.resources")
        )

      server.start(8051)

      val (status, headers, content) = HttpClient.get(new URI("http://localhost:8051/helloworld"))
      status should equal(200)
      content should equal("hello world")
    }

    it("should declare multi resource packages") {
      import Route._

      server = new Server(
        resources("org.megrez.server.http.netty.resources"),
        resources("org.megrez.server.http.netty.others")
        )

      server.start(8051)

      val (_, _, contentFromHelloWorld) = HttpClient.get(new URI("http://localhost:8051/helloworld"))
      contentFromHelloWorld should equal("hello world")

      val (_, _, contentFromHelloWorld2) = HttpClient.get(new URI("http://localhost:8051/helloworld2"))
      contentFromHelloWorld2 should equal("hello world 2")
    }

    it("should render template for resource if template specified") {
      import Route._


      server = new Server(
        resources("org.megrez.server.http.netty.resources")        
        )

      val url = classOf[ServerTest].getResource("/org/megrez/server/http/netty/resources/people")
      Representations.register[Person](new File(url.toURI))

      server.start(8051)

      val (_, _, content) = HttpClient.get(new URI("http://localhost:8051/people/lijian"))
      content should equal("<p>lijian</p>")
    }
  }

  var server: Server = null


  override protected def beforeEach() {
    server = null
  }

  override protected def afterEach() {
    if (server != null) server.shutdown
  }
}