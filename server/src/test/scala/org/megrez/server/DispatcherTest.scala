package org.megrez.server

import org.scalatest._
import org.scalatest.matchers._
import actors.Actor._
import java.util.UUID
import actors.{Actor, TIMEOUT}

class DispatcherTest extends Spec with ShouldMatchers with BeforeAndAfterEach with AgentTestSuite {
  describe("Dispatcher") {
    it("should assign job to idle agent") {
      agentConnect(agent)
      scheduleJob(UUID.randomUUID, job)
      expectAgentGotJob(job)
    }

    it("should notify scheduler about job completed") {
      agentConnect(agent)
      val buildId = UUID.randomUUID
      scheduleJob(buildId, job)
      expectAgentGotJob(job)

      jobFinishedOnAgent(buildId, job)

      expectJobCompleted
    }

  }

  def agentConnect(agents: Agent*) {
    agents.foreach(dispatcher ! AgentConnect(_))
  }

  def scheduleJob(id: UUID, job: Job): Unit = {
    dispatcher ! JobScheduled(id, Set(job))
  }

  def jobFinishedOnAgent(id: UUID, job: Job): Unit = {
    agent ! JobFinished(id, job, agent)
  }

  def expectJobCompleted: Unit = {
    receiveWithin(2000) {
      case _: JobCompleted =>
      case TIMEOUT => fail
      case msg: Any => println(msg); fail
    }
  }

  var job: Job = _
  var agent: Agent = _
  var dispatcher: Dispatcher = _

  override def beforeEach() {
    object Context {
      val buildScheduler = self
    }
    dispatcher = new Dispatcher(Context);
    job = new Job("unit test", Set(), List());    
    agent = new Agent(new ActorBasedAgentHandler(self), dispatcher)
    agent start;
    dispatcher start;
  }

  override def afterEach() {
    agent ! Exit();
    dispatcher ! Exit();
  }
}

class ActorBasedAgentHandler(val actor: Actor) extends AgentHandler {
  def send(message: String) {
    actor ! message
  }
  def assignAgent(agent : Actor) {}
}