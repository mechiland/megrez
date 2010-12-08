package org.megrez.server.core

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfterEach, BeforeAndAfterAll, Spec}
import org.megrez.server.model._
import data.{Plugin, Graph}
import org.neo4j.graphdb.Node
import trigger.OnChanges
import vcs.Subversion
import tasks.{Ant, CommandLine}
import java.io.File
import actors.Actor._
import actors.TIMEOUT
import org.megrez.server.{Neo4JSupport, IoSupport}

class IntegrationTest extends Spec with ShouldMatchers with BeforeAndAfterAll with BeforeAndAfterEach with IoSupport with Neo4JSupport {
  describe("Integration Test") {
    it("should trigger build between Trigger and BuilderScheduler") {

      val scheduler = new BuildScheduler(self)
      val trigger = new OnChanges(pipeline, workingDir, scheduler)
      trigger.start

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
    it("should assign job to idle agent when trigger build") {

      val dispatcher = new Dispatcher(null)
      dispatcher ! AgentConnect(self)

      val scheduler = new BuildScheduler(dispatcher)
      val trigger = new OnChanges(pipeline, workingDir, scheduler)
      trigger.start

      receiveWithin(1000) {
        case job: JobExecution =>
          List("author", "publish").contains(job.job.name)
        case TIMEOUT => fail
        case _ => fail
      }
    }
  }

  var material: Material = null
  var pipeline: Pipeline = null
  var author: Job = null
  var publish: Job = null
  var change: Change = null
  val workingDir = new File(System.getProperty("user.dir"))

  override def beforeEach() {
    val changeSource = ChangeSource(Map("type" -> "mock"))
    material = Material(Map("source" -> changeSource))
    author = Job(Map("name" -> "author", "tasks" -> List()))
    publish = Job(Map("name" -> "publish", "tasks" -> List()))
    val ut = Stage(Map("name" -> "UT", "jobs" -> List(author, publish)))
    pipeline = Pipeline(Map("name" -> "WGSN-bundles", "materials" -> List(material), "stages" -> List(ut)))
    change = Subversion.Revision(Map("revision" -> 42, "metarial" -> material))
  }

  override def beforeAll() {
    Neo4J.start
    Graph.of(neo).consistOf(Task, Job, Stage, ChangeSource, Material, Pipeline, Build, StageExecution, JobExecution)
    Graph.consistOf(CommandLine, Ant, Subversion, Subversion.Revision)
    Graph.consistOf(MockChangeSource)
  }

  override def afterAll() {
    Neo4J.shutdown
  }

  class MockChangeSource private(val node: Node) extends ChangeSource {
    def getChange(workingDir: File, material: Material) =
      Some(change)

    def changes(workingDir: File, previous: Option[Any]) = None

    def toChangeSource = null
  }

  object MockChangeSource extends Plugin(ChangeSource, "mock") {
    def apply(node: Node) = new MockChangeSource(node)
  }
}