package org.megrez.agent

import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import java.net.URI
import actors.Actor
import actors.Actor._

class ServerTest extends Spec with ShouldMatchers {
  describe("Agent Server Connection") {
    it("should connect to server if server response ws handshake and megrez handshake") {
      val server = new TestServer
      server.response(WebSocketHandshake, MegrezHandshake)
      server.start
      val connection = new Server(new URI("ws://localhost:8080/")) with ActorBasedServerHandler
      connection.actor = self
      connection.connect

      receiveWithin(1000) {
        case "CONNECTED" => server.shutdown
        case other : String => {
          println(other)
          server.shutdown
          fail
        }
      }
    }

    it("should report not a megrez server if server not repsonse to websocket upgrade") {
      val server = new TestServer
      server.response(Forbidden)
      server.start
      
      val connection = new Server(new URI("ws://localhost:8080/")) with ActorBasedServerHandler
      connection.actor = self
      connection.connect

      receiveWithin(1000) {
        case "NOT A MERGEZ SERVER" => server.shutdown
        case _ => {
          server.shutdown
          fail
        }
      }
    }

    it("should report not a megrez server if server not response to the megrez handshake") {
      val server = new TestServer
      server.response(WebSocketHandshake, Something)
      server.start

      val connection = new Server(new URI("ws://localhost:8080/")) with ActorBasedServerHandler
      connection.actor = self
      connection.connect

      receiveWithin(1000) {
        case "NOT A MERGEZ SERVER" => server.shutdown
        case _ => {
          server.shutdown
          fail
        }
      }
    }
  }
}

trait ActorBasedServerHandler extends ServerHandler {
  var actor : Actor = _
  override def megrezServerConnected() {
    actor ! "CONNECTED"
  }

  override def invalidMegrezServer(uri : URI) {
    actor ! "NOT A MERGEZ SERVER"
  }
}