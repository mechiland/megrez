package org.megrez.server.core

import org.scalatest.{Spec, BeforeAndAfterAll}
import org.scalatest.matchers.ShouldMatchers
import org.megrez.server.model._
import org.megrez.server.model.data.Graph
import tasks.{Ant, CommandLine}
import vcs.Subversion
import scala.actors.Actor._
import scala.actors.TIMEOUT
import org.megrez.JobAssignmentFuture
import org.megrez.server.{IoSupport, Neo4JSupport}

class DispatcherTest extends Spec with ShouldMatchers with BeforeAndAfterAll with IoSupport with Neo4JSupport {
  describe("Dispatcher") {
    it("should assign job to the idle agent") {
      val change = Subversion.Revision(Map("revision" -> 1, "metarial" -> material))

      val dispatcher = new Dispatcher(null)
      val build = Build(pipeline, Set(change))
      dispatcher ! AgentConnect(self)
      dispatcher ! build.next.map((build, _))

      receiveWithin(1000) {
        case jobAssignment: JobAssignmentFuture =>
          jobAssignment.pipeline should equal("WGSN-bundles")
          reply(AgentToDispatcher.Confirm)
        case TIMEOUT => fail
        case _ => fail
      }
    }

    it("should assign job when agent connect") {
      val change = Subversion.Revision(Map("revision" -> 1, "metarial" -> material))

      val dispatcher = new Dispatcher(null)
      val build = Build(pipeline, Set(change))
      dispatcher ! build.next.map((build, _))
      dispatcher ! AgentConnect(self)

      receiveWithin(1000) {
        case jobAssignment: JobAssignmentFuture =>
          jobAssignment.pipeline should equal("WGSN-bundles")
          reply(AgentToDispatcher.Confirm)
        case TIMEOUT => fail
        case _ => fail
      }
    }

    it("should try next agent if the job is rejected") {
      val change = Subversion.Revision(Map("revision" -> 1, "metarial" -> material))

      val dispatcher = new Dispatcher(null)
      val build = Build(pipeline, Set(change))

      val agent = actor {
        react {
          case jobAssignment: JobAssignmentFuture =>
            reply(AgentToDispatcher.Reject)
          case TIMEOUT => fail
          case _ => fail
        }
      }

      dispatcher ! AgentConnect(agent)
      dispatcher ! AgentConnect(self)

      dispatcher ! build.next.map((build, _))

      receiveWithin(1000) {
        case jobAssignment: JobAssignmentFuture =>
          jobAssignment.pipeline should equal("WGSN-bundles")
          reply(AgentToDispatcher.Confirm)
        case TIMEOUT => fail
        case _ => fail
      }
    }

    it("should send the job status to sheduler and do another assigning after job finished") {
      val change = Subversion.Revision(Map("revision" -> 1, "metarial" -> material))
      val build = Build(pipeline, Set(change))

      val dispatcher = new Dispatcher(self)
      dispatcher ! AgentConnect(self)
      dispatcher ! build.next.map((build, _))

      receiveWithin(1000) {
        case job: JobAssignmentFuture =>
          job.pipeline should equal("WGSN-bundles")
          reply(AgentToDispatcher.Confirm)
          dispatcher ! AgentToDispatcher.JobFinished(self, job)
        case TIMEOUT => fail
        case _ => fail
      }

      receiveWithin(1000) {
        case JobFinished(build, operation) =>
          build.pipeline.name should equal("WGSN-bundles")
        case TIMEOUT => fail
        case _ => fail
      }

    }
  }

  var pipeline: Pipeline = null
  var material: Material = null
  var packageJob: Job = null

  override def beforeAll() {
    Neo4J.start
    Graph.of(neo).consistOf(Task, Job, Stage, ChangeSource, Material, Pipeline, Build, StageExecution, JobExecution)
    Graph.consistOf(CommandLine, Ant, Subversion, Subversion.Revision)

    val changeSource = ChangeSource(Map("type" -> "svn", "url" -> "svn_url"))
    material = Material(Map("source" -> changeSource))
    packageJob = Job(Map("name" -> "package", "tasks" -> List()))
    val packageStage = Stage(Map("name" -> "package", "jobs" -> List(packageJob)))
    pipeline = Pipeline(Map("name" -> "WGSN-bundles", "materials" -> List(material), "stages" -> List(packageStage)))
  }

  override def afterAll() {
    Neo4J.shutdown
  }
}
