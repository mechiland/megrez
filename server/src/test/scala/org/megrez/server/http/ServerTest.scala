package org.megrez.server.http

import jersey.resources.Person
import org.scalatest.{Spec, BeforeAndAfterEach}
import org.scalatest.matchers.ShouldMatchers
import org.megrez.server.HttpClientSupport
import java.net.URI
import java.io.File
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame
import org.jboss.netty.channel.{ChannelHandlerContext, MessageEvent, SimpleChannelUpstreamHandler, Channel}
import actors.Actor._
import actors.{TIMEOUT, Actor}

class ServerTest extends Spec with ShouldMatchers with BeforeAndAfterEach with HttpClientSupport {
  describe("Server") {
    it("should declare resource package") {
      import Route._

      server = new Server(
        resources("org.megrez.server.http.jersey.resources")
        )

      server.start(8051)

      val (status, headers, content) = HttpClient.get(new URI("http://localhost:8051/helloworld"))
      status should equal(200)
      content should equal("hello world")
    }

    it("should declare multi resource packages") {
      import Route._

      server = new Server(
        resources("org.megrez.server.http.jersey.resources"),
        resources("org.megrez.server.http.jersey.others")
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
        resources("org.megrez.server.http.jersey.resources")
        )

      val url = classOf[ServerTest].getResource("/org/megrez/server/http/jersey/resources/people")
      Representations.register[Person](new File(url.toURI))

      server.start(8051)

      val (_, _, content) = HttpClient.get(new URI("http://localhost:8051/people/lijian"))
      content should equal("<p>lijian</p>")
    }

    it("should declare websocket") {
      import Route._

      def agent(channel: Channel, actor: Actor) = new SimpleChannelUpstreamHandler() {
        override def messageReceived(context: ChannelHandlerContext, event: MessageEvent) {
          event.getMessage match {
            case frame: WebSocketFrame =>
              if (!frame.isBinary) actor ! frame.getTextData
            case _ =>
          }
        }
      }

      server = new Server(
        websocket("/agent", agent, self)
        )

      server.start(8051)

      val client = new WebSocketClient(new URI("ws://localhost:8051/agent"), self)

      try {
        receiveWithin(1000) {
          case "megrez-agent:1.0" =>
          case TIMEOUT => fail
          case _ => fail
        }
      } finally {
        client.shutdown
      }
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