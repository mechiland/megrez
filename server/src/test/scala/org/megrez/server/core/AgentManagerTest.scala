package org.megrez.server.core

import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import actors.Actor._
import org.scalatest.mock.MockitoSugar
import org.mockito.ArgumentCaptor
import actors.{TIMEOUT, Actor}
import org.mockito.Mockito._
import org.megrez.server.{AgentManagerToDispatcher, ToAgentManager, AgentHandler}

class AgentManagerTest extends Spec with ShouldMatchers with MockitoSugar {
  describe("Agent management") {
    it("should notify dispatcher remote agent connected") {
      val handler = mock[AgentHandler]
      val manager = new AgentManager(self)

      manager ! ToAgentManager.RemoteAgentConnected(handler)

      val assignedAgent = ArgumentCaptor.forClass(classOf[Actor])

      receiveWithin(1000) {
        case AgentManagerToDispatcher.AgentConnect(agent : Actor) =>
          verify(handler).assignAgent(assignedAgent.capture)
          agent should be === assignedAgent.getValue
        case TIMEOUT => fail
        case _ => fail
      }
    }
  }
}