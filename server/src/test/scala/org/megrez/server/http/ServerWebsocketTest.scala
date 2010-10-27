package org.megrez.server.http

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Spec}
import scala.actors.Actor._
import actors.TIMEOUT
import java.net.URI

import Route._

class ServerWebsocketTest extends Spec with ShouldMatchers with BeforeAndAfterEach with MockitoSugar {
  describe("Server websocket support") {
    it("should ") {
      server = new Server(
        websocket("/agent") -> self
      )

      server.start(8080)

      client = new WebSocketClient(new URI("ws://localhost:8080/agent"))

      receiveWithin(1000) {
        case "ACTOR CONNECTED" =>
        case TIMEOUT => fail
        case _ => fail
      }
    }
  }

  var server : Server = _
  var client : WebSocketClient = _
  
  override protected def afterEach() {
    server.shutdown
    client.shutdown
  }
}