package org.megrez.agent

import org.scalatest.matchers.ShouldMatchers
import java.net.URI
import actors.Actor._
import org.scalatest._
import actors._

class ServerTest extends ServerIntegration with ShouldMatchers {
  describe("Agent Server handshake") {
    it("should connect to server if server response ws handshake and megrez handshake") {
      server.response(WebSocketHandshake, MegrezHandshake)
      server.start
      connect
      expect("CONNECTED", 1000)
    }

    it("should report not a megrez server if server not repsonse to websocket upgrade") {
      server.response(Forbidden)
      server.start
      connect
      expect("NOT A MEGREZ SERVER", 1000)
    }

    it("should report not a megrez server if server not response to the megrez handshake") {
      server.response(WebSocketHandshake, Something)
      server.start
      connect
      expect("NOT A MEGREZ SERVER", 1000)
    }
  }

  describe("retry server connection") {
    it("should retry connecting to server") {
      server.response(WebSocketHandshake, CloseAfterMegrezHandshake, WebSocketHandshake, MegrezHandshake)
      server.start
      connect
      expect("CONNECTED", 1000)
      expect("DISCONNECTED", 1000)
      expect("CONNECTED", 2000)
    }
  }  
}
