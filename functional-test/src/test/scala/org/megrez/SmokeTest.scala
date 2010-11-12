package org.megrez

import org.scalatest.matchers.ShouldMatchers

import java.io.File
import org.scalatest.{BeforeAndAfterEach, Spec}
import actors.TIMEOUT
import actors.Actor._

class SmokeTest extends Spec with ShouldMatchers with BeforeAndAfterEach {  
  describe("Smoke test") {
    it("should assign job to agent when source code changes") {
      startServer()
      startAgent()
      receiveWithin(5000) {
        case TIMEOUT =>
        case _ => fail
      }
    }
  }

  val Client = org.megrez.agent.Main
  val Server = org.megrez.server.Main

  private def startAgent() {
    Client.start("ws://localhost:8080/agent", agentWorkingDir)
  }

  private def startServer() {    
    Server.start(8080)    
  }

  var root : File = _
  var agentWorkingDir : File = _

  override def beforeEach() {
    root = new File(System.getProperty("user.dir"), "target/functional-test")
    agentWorkingDir = new File(root, "agent")
  }

}