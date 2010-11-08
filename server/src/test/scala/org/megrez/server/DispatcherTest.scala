package org.megrez.server

import org.scalatest._
import org.scalatest.matchers._
import actors.Actor._
import java.util.UUID
import actors.{Actor, TIMEOUT}
import org.megrez.{JobAssignment, Material, Job}

class DispatcherTest extends Spec with ShouldMatchers with BeforeAndAfterEach with AgentTestSuite {
  describe("Dispatcher") {
    it("should assign job to idle agent") {
      val job = new Job("unit test", Set(), List())
      val assignment: JobAssignment = JobAssignment("pipeline", Map[Material, Option[Any]](), job)

      val dispatcher = new Dispatcher(Context)
      dispatcher ! AgentConnect(self)

      dispatcher ! JobScheduled(UUID.randomUUID, Set(assignment))
      receiveWithin(1000) {
        case jobAssignment: JobAssignment =>
          jobAssignment should be === assignment
          reply(AgentToDispatcher.Confirm)
        case TIMEOUT => fail
        case _ => fail
      }
    }

    it("should try next agent if the first one reject the job") {
      val job = new Job("unit test", Set(), List())
      val assignment: JobAssignment = JobAssignment("pipeline", Map[Material, Option[Any]](), job)
      val main = self
      val agent1 = actor {
        react {
          case jobAssignment: JobAssignment =>
            reply(AgentToDispatcher.Reject)
          case TIMEOUT => fail
          case _ => fail
        }
      }

      val agent2 = actor {
        react {
          case jobAssignment: JobAssignment =>
            main ! jobAssignment
            reply(AgentToDispatcher.Confirm)
          case TIMEOUT => fail
          case _ => fail
        }
      }      

      val dispatcher = new Dispatcher(Context)
      dispatcher ! AgentConnect(agent1)
      dispatcher ! AgentConnect(agent2)

      dispatcher ! JobScheduled(UUID.randomUUID, Set(assignment))

      receiveWithin(1000) {
        case jobAssignment: JobAssignment =>
          jobAssignment should be === assignment
        case TIMEOUT => fail
        case _ => fail
      }
    }

    //    it("should assign job to idle agent") {
    //      agentsConnect(self)
    //      scheduleJobRequest(jobRequest)
    //
    //      expectAgentGotJobRequests(jobRequest)
    //    }
    //
    //    it("should notify scheduler about job completed") {
    //      agentsConnect(agent)
    //      scheduleJobRequest(jobRequest)
    //      expectAgentGotJobRequests(jobRequest)
    //
    //      jobFinishedOnAgent(jobRequest)
    //
    //      expectJobCompleted
    //    }
    //
    //    it("should assign job not to more than one resource matched agent") {
    //      val agent2 = createAndStartAnAgent
    //      agentsConnect(agent, agent2)
    //
    //      scheduleJobRequest(jobRequest)
    //      var jobRequest2 = JobRequest(UUID.randomUUID, new Job("unit test2", Set(), List()))
    //      scheduleJobRequest(jobRequest2)
    //      expectAgentGotJobRequests(jobRequest, jobRequest2)
    //      agent2 ! Exit();
    //    }
    //
    //    it("should begin assigning jobs when idle agents connected") {
    //      scheduleJobRequest(jobRequest)
    //      agentsConnect(agent)
    //      expectAgentGotJobRequests(jobRequest)
    //    }
    //
    //    it("should begin assigning jobs when some job finished") {
    //      agentsConnect(agent)
    //      scheduleJobRequest(jobRequest)
    //      var jobRequest2 = JobRequest(UUID.randomUUID, new Job("unit test2", Set("LINUX"), List()))
    //      scheduleJobRequest(jobRequest2)
    //      expectAgentGotJobRequests(jobRequest)
    //
    //      agent ! SetResources(Set("LINUX"))
    //
    //      jobFinishedOnAgent(jobRequest)
    //      expectJobCompleted
    //
    //      expectAgentGotJobRequests(jobRequest2)
    //    }
  }

  object Context {
    val buildScheduler = self
  }

  def agentsConnect(agents: Agent*) {
    agents.foreach(dispatcher ! AgentConnect(_))
  }

  def scheduleJobRequest(jobRequest: JobRequest): Unit = {
    dispatcher ! JobScheduled(jobRequest.buildId, Set(JobAssignment("pipelinw", Map[Material, Option[Any]](), jobRequest.job)))
  }

  def jobFinishedOnAgent(jobRequest: JobRequest): Unit = {
    agent ! JobFinished(jobRequest.buildId, jobRequest.job, agent)
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

  var jobRequest: JobRequest = _
  var agent: Agent = _
  var dispatcher: Dispatcher = _

  override def beforeEach() {
    object Context {
      val buildScheduler = self
    }
    dispatcher = new Dispatcher(Context);
    jobRequest = JobRequest(UUID.randomUUID, new Job("unit test", Set(), List()));
    agent = createAndStartAnAgent
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

  def assignAgent(agent: Actor) {}
}