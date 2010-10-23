package org.megrez.agent

import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import java.net.URI
import actors.Actor
import actors.Actor._

class ServerConnectionTest extends Spec with ShouldMatchers {
  describe("Agent Server Connection") {
    it("should connect to server if server response ws upgrade") {
      val server = new TestServer with ResponseHandshake
      server.start
      val connection = new ServerConnection(new URI("ws://localhost:8080/")) with ActorBasedWorkerController
      connection.actor = self
      connection.connect

      receiveWithin(1000) {
        case "CONNECTED" => 
        case _ => fail
      }
    }
  }
}

trait ActorBasedWorkerController extends WorkerController {
  var actor : Actor = _
  override def serverConnected() {
    actor ! "CONNECTED"
  }
}