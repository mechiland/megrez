package org.megrez.agent

import org.scalatest.matchers.ShouldMatchers
import java.net.URI

import actors.Actor
import actors.Actor._
import org.scalatest.{BeforeAndAfterEach, Spec}


class HandshakeHandlerTest extends Spec with ShouldMatchers with BeforeAndAfterEach {
  describe("Agent-Server handshake") {
    it("should notify server connected if server response websocket handshake and megrez handshake") {
      server.response(WebSocketHandshake, MegrezHandshake)
      server.start
      connect
      expect("CONNECTED", 1000)
    }

    it("should notify invalid server if server refuse the websocket connection") {
      server.response(Forbidden)
      server.start
      connect
      expect("NOT A MERGEZ SERVER", 1000)
    }

    it("should notify invalid server if server does not response megrez handshake") {
      server.response(WebSocketHandshake, Something)
      server.start
      connect      
      expect("NOT A MERGEZ SERVER", 1000)      
    }
  }

  def connect {
    val client = new WebSocketClient(new HandshakeHandler(new URI("ws://localhost:8080/"), new ActorBasedServerHandler(self)))
  }  

  def expect(Message : String, timeout : Int) {
    receiveWithin(timeout) {
      case Message =>
      case _ => fail
    }
  }

  var server: WebSocketServer = _

  override def beforeEach() {
    server = new WebSocketServer
  }

  override def afterEach() {
    server.shutdown
  }
}