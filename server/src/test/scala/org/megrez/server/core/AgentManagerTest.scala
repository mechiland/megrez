package org.megrez.server.core

import org.scalatest.mock.MockitoSugar
import org.megrez.server.{IoSupport, Neo4JSupport}
import org.scalatest.{Spec, BeforeAndAfterAll}
import org.scalatest.matchers.ShouldMatchers
import actors.Actor._
import actors.TIMEOUT
import org.megrez.server.http.AgentWebSocketHandler
import org.jboss.netty.channel.Channel
import org.megrez.server.model.data.Graph
import org.megrez.server.model.Agent

class AgentManagerTest extends Spec with ShouldMatchers with BeforeAndAfterAll with IoSupport with Neo4JSupport with MockitoSugar {
  describe("AgentManager") {
    it("should send agent connect to dispatcher when agent connected") {
      val agentManager = new AgentManager(self)

      val handler = new AgentWebSocketHandler(mock[Channel], agentManager)
      agentManager ! ToAgentManager.RemoteAgentConnected(handler)

      receiveWithin(1000) {
        case AgentManagerToDispatcher.AgentConnect(agent) =>
        case TIMEOUT => fail
        case _ => fail
      }
    }
  }

  override def beforeAll() {
    Neo4J.start
    Graph.of(neo).consistOf(Agent)
 }

  override def afterAll() {
    Neo4J.shutdown
  }
}