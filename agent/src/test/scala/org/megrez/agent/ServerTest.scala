package org.megrez.agent

import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import java.net.URI
import actors.Actor
import actors.Actor._

class ServerTest extends Spec with ShouldMatchers {
  describe("Agent Server handshake") {
    it("should connect to server if server response ws handshake and megrez handshake") {
      val server = new WebSocketServer
      server.response(WebSocketHandshake, MegrezHandshake)
      server.start
      val connection = new Server(new URI("ws://localhost:8080/")) with ActorBasedServerHandlerMixin
      connection.actor = self
      connection.connect

      receiveWithin(1000) {
        case "CONNECTED" => server.shutdown
        case other : String => {
          server.shutdown
          fail
        }
      }
    }

    it("should report not a megrez server if server not repsonse to websocket upgrade") {
      val server = new WebSocketServer
      server.response(Forbidden)
      server.start
      
      val connection = new Server(new URI("ws://localhost:8080/")) with ActorBasedServerHandlerMixin
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
      val server = new WebSocketServer
      server.response(WebSocketHandshake, Something)
      server.start

      val connection = new Server(new URI("ws://localhost:8080/")) with ActorBasedServerHandlerMixin
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
