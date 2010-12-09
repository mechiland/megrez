package org.megrez.server.http.netty

import org.scalatest.{Spec, BeforeAndAfterEach}
import org.scalatest.matchers.ShouldMatchers
import org.megrez.server.HttpClientSupport
import java.net.URI

class ServerTest extends Spec with ShouldMatchers with BeforeAndAfterEach with HttpClientSupport {
  describe("Server") {
    it("should declare rest package") {
      import Route._

      server = new Server(
        resources("org.megrez.server.http.netty.resources")
        )

      server.start(8051)

      val (status, headers, content) = HttpClient.get(new URI("http://localhost:8051/helloworld"))
      status should equal(200)
      content should equal("hello world")
    }
  }

  var server: Server = null

  override protected def afterEach() {
    if (server != null) server.shutdown
  }
}