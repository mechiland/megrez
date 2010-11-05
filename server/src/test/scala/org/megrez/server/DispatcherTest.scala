package org.megrez.server

import org.scalatest._
import org.scalatest.matchers._
import actors.Actor._
import java.util.UUID
import actors.{Actor, TIMEOUT}

class DispatcherTest extends Spec with ShouldMatchers with BeforeAndAfterEach with AgentTestSuite {
  describe("Dispatcher") {
    it("should assign job to idle agent") {
      agentsConnect(agent)
      scheduleJob(UUID.randomUUID, job)
      expectAgentGotJobs(job)
    }

    it("should notify scheduler about job completed") {
      agentsConnect(agent)
      val buildId = UUID.randomUUID
      scheduleJob(buildId, job)
      expectAgentGotJobs(job)

      jobFinishedOnAgent(buildId, job)

      expectJobCompleted
    }

    it("should assign job not to more than one resource matched agent") {
      val agent2 = createAndStartAnAgent
      agentsConnect(agent, agent2)

      scheduleJob(UUID.randomUUID, job)
      var job2 = new Job("unit test2", Set(), List())
      scheduleJob(UUID.randomUUID, job2)
      expectAgentGotJobs(job, job2)
      agent2 ! Exit();
    }
  }

  def agentsConnect(agents: Agent*) {
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

  def createAndStartAnAgent: Agent = {
    var agent = new Agent(new ActorBasedAgentHandler(self), dispatcher)
    agent start;
    agent
  }

  var job: Job = _
  var agent: Agent = _
  var dispatcher: Dispatcher = _

  override def beforeEach() {
    dispatcher = new Dispatcher();
    job = new Job("unit test", Set(), List());
    dispatcher.buildScheduler = self
    agent = createAndStartAnAgent
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