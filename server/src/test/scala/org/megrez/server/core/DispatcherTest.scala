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
import org.megrez.server.{AgentToDispatcher, IoSupport, Neo4JSupport}

class DispatcherTest extends Spec with ShouldMatchers with BeforeAndAfterAll with IoSupport with Neo4JSupport {
  describe("Dispatcher") {
    it("should assign job to the idle agent") {
      val change = Subversion.Revision(Map("revision" -> 1, "metarial" -> material))

      val dispatcher = new Dispatcher(null)
      val build = Build(pipeline, Set(change))
      dispatcher ! AgentConnect(self)
      dispatcher ! build.next.map((build, _))

      receiveWithin(1000) {
        case jobAssignment: JobAssignmentFuture => jobAssignment.pipeline should equal("WGSN-bundles")
        case TIMEOUT => fail
        case _ => fail
      }
    }
    it("should send JobFinished message to scheduler if agent finished job") {
      val change = Subversion.Revision(Map("revision" -> 1, "metarial" -> material))
      val build = Build(pipeline, Set(change))

      val dispatcher = new Dispatcher(self)
      val assignJobs: List[Pair[Build, JobExecution]] = build.next.map((build, _))

      dispatcher ! AgentConnect(self)
      dispatcher ! assignJobs

      receiveWithin(1000) {
        case jobAssignment: JobAssignmentFuture => jobAssignment.pipeline should equal("WGSN-bundles")
        reply(AgentToDispatcher.Confirm)
        dispatcher.assignedJobs.put(1, assignJobs.head._2)
        dispatcher ! AgentToDispatcher.JobFinished(self, new JobAssignmentFuture(1, "WGSN-bundles", null, null))
        case TIMEOUT => fail
        case _ => fail
      }

      receiveWithin(5000) {
        case JobFinished(build, operation) => build.pipeline.name should equal("WGSN-bundles")
        case TIMEOUT => fail
        case any: Any => println(any.toString)
      }
    }
  }

  var pipeline: Pipeline = null
  var material: Material = null
  var change: Change = null
  var author: Job = null
  var publish: Job = null
  var packageJob: Job = null

  override def beforeAll() {
    Neo4J.start
    Graph.of(neo).consistOf(Task, Job, Stage, ChangeSource, Material, Pipeline, Build, StageExecution, JobExecution)
    Graph.consistOf(CommandLine, Ant, Subversion, Subversion.Revision)

    val changeSource = ChangeSource(Map("type" -> "svn", "url" -> "svn_url"))
    material = Material(Map("source" -> changeSource))
    author = Job(Map("name" -> "author", "tasks" -> List()))
    publish = Job(Map("name" -> "publish", "tasks" -> List()))
    packageJob = Job(Map("name" -> "package", "tasks" -> List()))
    val ut = Stage(Map("name" -> "UT", "jobs" -> List(author, publish)))
    val packageStage = Stage(Map("name" -> "package", "jobs" -> List(packageJob)))
    pipeline = Pipeline(Map("name" -> "WGSN-bundles", "materials" -> List(material), "stages" -> List(ut, packageStage)))
  }

  override def afterAll() {
    Neo4J.shutdown
  }
}