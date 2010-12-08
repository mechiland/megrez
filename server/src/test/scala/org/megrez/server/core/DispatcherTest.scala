package org.megrez.server.core

import org.scalatest.{Spec, BeforeAndAfterAll}
import org.scalatest.matchers.ShouldMatchers
import org.megrez.server.model._
import org.megrez.server.model.data.Graph
import tasks.{Ant, CommandLine}
import vcs.Subversion
import scala.actors.Actor._
import scala.actors.TIMEOUT
import org.megrez.server.{IoSupport, Neo4JSupport}
import org.megrez.Stop

class DispatcherTest extends Spec with ShouldMatchers with BeforeAndAfterAll with IoSupport with Neo4JSupport {
  describe("Dispatcher") {
    it("should assign job to the idle agent") {
      val change = Subversion.Revision(Map("revision" -> 1, "metarial" -> material))

      val dispatcher = new Dispatcher(null)
      val build = Build(simplePipeline, Set(change))
      dispatcher ! AgentConnect(self)
      dispatcher ! build.next.map((build, _))

      receiveWithin(1000) {
        case jobExecution: JobExecution =>
          jobExecution.job.name should equal("package")
          reply(AgentToDispatcher.Confirm)
        case TIMEOUT => fail
        case _ => fail
      }
    }

    it("should assign job when agent connect") {
      val change = Subversion.Revision(Map("revision" -> 1, "metarial" -> material))

      val dispatcher = new Dispatcher(null)
      val build = Build(simplePipeline, Set(change))
      dispatcher ! build.next.map((build, _))
      dispatcher ! AgentConnect(self)

      receiveWithin(1000) {
        case jobExecution: JobExecution =>
          jobExecution.job.name should equal("package")
          reply(AgentToDispatcher.Confirm)
        case TIMEOUT => fail
        case _ => fail
      }
    }

    it("should try next agent if the job is rejected") {
      val change = Subversion.Revision(Map("revision" -> 1, "metarial" -> material))

      val dispatcher = new Dispatcher(null)
      val build = Build(simplePipeline, Set(change))

      val agent = actor {
        react {
          case jobExecution: JobExecution =>
            reply(AgentToDispatcher.Reject)
          case TIMEOUT => fail
          case _ => fail
        }
      }

      dispatcher ! AgentConnect(agent)
      dispatcher ! AgentConnect(self)

      dispatcher ! build.next.map((build, _))

      receiveWithin(1000) {
        case jobExecution: JobExecution =>
          jobExecution.job.name should equal("package")
          reply(AgentToDispatcher.Confirm)
        case TIMEOUT => fail
        case _ => fail
      }
    }

    it("should notify sheduler when job finished") {
      val change = Subversion.Revision(Map("revision" -> 1, "metarial" -> material))
      val build = Build(simplePipeline, Set(change))

      val dispatcher = new Dispatcher(self)
      dispatcher ! AgentConnect(self)
      dispatcher ! build.next.map((build, _))

      receiveWithin(1000) {
        case jobExecution: JobExecution =>
          jobExecution.job.name should equal("package")
          reply(AgentToDispatcher.Confirm)
          dispatcher ! AgentToDispatcher.JobFinished(self, jobExecution)
        case TIMEOUT => fail
        case _ => fail
      }

      receiveWithin(1000) {
        case DispatcherToScheduler.JobFinished(build, operation) =>
          build.pipeline.name should equal("WGSN-bundles")
        case TIMEOUT => fail
        case _ => fail
      }

    }
  }

  describe("Dispatcher with multiple jobs and agents in queue") {
    it("should notify sheduler and trigger another job assigning when job finished") {
      val change = Subversion.Revision(Map("revision" -> 1, "metarial" -> material))
      val build = Build(complexPipeline, Set(change))
      val main = self
      object Free

      val agent = actor {
        var isBusy = false
        loop {
          react {
            case jobExecution: JobExecution =>
              if (!isBusy) {
                reply(AgentToDispatcher.Confirm)
                main ! jobExecution
                isBusy = true
              } else reply(AgentToDispatcher.Reject)
            case Free =>
              isBusy = false
              reply(Free)
            case TIMEOUT => fail
            case _ => fail
          }
        }
      }

      val dispatcher = new Dispatcher(main)
      dispatcher ! AgentConnect(agent)
      dispatcher ! build.next.map((build, _))

      receiveWithin(1000) {
        case jobExecution: JobExecution =>
          jobExecution.job.name should equal("compile")
          agent !? Free match {
            case Free =>
              dispatcher ! AgentToDispatcher.JobFinished(self, jobExecution)
          }
        case TIMEOUT => fail
        case _ => fail
      }

      receiveWithin(1000) {
        case DispatcherToScheduler.JobFinished(build, operation) =>
          build.pipeline.name should equal("Complex Pipeline")
        case TIMEOUT => fail
        case _ => fail
      }

      receiveWithin(1000) {
        case jobExecution: JobExecution =>
          jobExecution.job.name should equal("unitTest")
        case TIMEOUT => fail
        case _ => fail
      }

    }
  }

  var simplePipeline: Pipeline = null
  var complexPipeline: Pipeline = null
  var material: Material = null

  override def beforeAll() {
    Neo4J.start
    Graph.of(neo).consistOf(Task, Job, Stage, ChangeSource, Material, Pipeline, Build, StageExecution, JobExecution)
    Graph.consistOf(CommandLine, Ant, Subversion, Subversion.Revision)

    val changeSource = ChangeSource(Map("type" -> "svn", "url" -> "svn_url"))
    material = Material(Map("source" -> changeSource))
    val packageJob = Job(Map("name" -> "package", "tasks" -> List()))
    val packageStage = Stage(Map("name" -> "package", "jobs" -> List(packageJob)))
    simplePipeline = Pipeline(Map("name" -> "WGSN-bundles", "materials" -> List(material), "stages" -> List(packageStage)))

    val compileJob = Job(Map("name" -> "compile", "tasks" -> List()))
    val unitTestJob = Job(Map("name" -> "unitTest", "tasks" -> List()))
    val buildStage = Stage(Map("name" -> "build", "jobs" -> List(compileJob, unitTestJob)))
    complexPipeline = Pipeline(Map("name" -> "Complex Pipeline", "materials" -> List(material), "stages" -> List(buildStage, packageStage)))
  }

  override def afterAll() {
    Neo4J.shutdown
  }
}
