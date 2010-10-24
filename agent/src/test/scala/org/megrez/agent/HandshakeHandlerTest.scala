package org.megrez.agent

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import java.net.URI

import actors.Actor
import actors.Actor._


class HandshakeHandlerTest extends Spec with ShouldMatchers {
  describe("Agent-Server handshake") {
    it("should notify server connected if server response websocket handshake and megrez handshake") {
      val server = new WebSocketServer
      server.response(WebSocketHandshake, MegrezHandshake)
      server.start
      val client = new WebSocketClient(new HandshakeHandler(new URI("ws://localhost:8080/"), new ActorBasedServerHandler(self)))

      receiveWithin(1000) {
        case "CONNECTED" => server.shutdown
        case other : String => {
          server.shutdown
          fail
        }
      }
    }

    it("should notify invalid server if server refuse the websocket connection") {
      val server = new WebSocketServer
      server.response(Forbidden)
      server.start
      val client = new WebSocketClient(new HandshakeHandler(new URI("ws://localhost:8080/"), new ActorBasedServerHandler(self)))
      
      receiveWithin(1000) {
        case "NOT A MERGEZ SERVER" => server.shutdown
        case other : String => {
          server.shutdown
          fail
        }
      }
    }

    it("should notify invalid server if server does not response megrez handshake") {
      val server = new WebSocketServer
      server.response(WebSocketHandshake, Something)
      server.start
      val client = new WebSocketClient(new HandshakeHandler(new URI("ws://localhost:8080/"), new ActorBasedServerHandler(self)))

      receiveWithin(1000) {
        case "NOT A MERGEZ SERVER" => server.shutdown
        case other : String => {
          server.shutdown
          fail
        }
      }
    }

  }
}