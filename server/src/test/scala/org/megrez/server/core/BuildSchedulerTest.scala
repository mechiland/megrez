package org.megrez.server.core

import org.scalatest.matchers.ShouldMatchers
import org.megrez.server.{IoSupport, Neo4JSupport}
import org.scalatest.{BeforeAndAfterAll, Spec}
import org.megrez.server.model.data.Graph
import org.megrez.server.model._
import tasks.{Ant, CommandLine}
import vcs.Subversion
import scala.actors.Actor._
import scala.actors.TIMEOUT

class BuildSchedulerTest extends Spec with ShouldMatchers with BeforeAndAfterAll with IoSupport with Neo4JSupport {
  describe("Build scheduler") {
    it("should send job assignment to dispatcher for the first stage") {
      val change = Subversion.Revision(Map("revision" -> 1, "metarial" -> material))

      val scheduler = new BuildScheduler(self)
      scheduler ! TriggerBuild(pipeline, Set(change))

      receiveWithin(1000) {
        case jobs: List[Pair[Build, JobExecution]] =>
          jobs should have size (2)
          jobs.head._1.changes should equal(Set(change))
          jobs.head._2.job should equal(author)
          jobs.last._1.changes should equal(Set(change))
          jobs.last._2.job should equal(publish)
        case TIMEOUT => fail
        case _ => fail
      }
    }

    it("should schedule jobs for next stage if the first stage job all completed") {
      val change = Subversion.Revision(Map("revision" -> 1, "metarial" -> material))

      val scheduler = new BuildScheduler(self)
      scheduler ! TriggerBuild(pipeline, Set(change))

      receiveWithin(1000) {
        case jobs: List[Pair[Build, JobExecution]] =>
          jobs.foreach {
            pair =>
              pair._2.completed
              scheduler ! JobFinishedOnBuild(pair._1)
          }
        case TIMEOUT => fail
        case _ => fail
      }

      receiveWithin(1000) {
        case jobs: List[Pair[Build, JobExecution]] =>
          jobs should have size (1)
          jobs.head._2.job should equal(packageJob)
        case TIMEOUT => fail
        case _ => fail
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