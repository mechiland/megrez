package org.megrez.server.core.trigger

import actors.Actor._
import org.megrez.server.{IoSupport, Neo4JSupport}
import org.scalatest.matchers.ShouldMatchers
import org.megrez.server.model._
import data.{Plugin, Graph}
import org.megrez.server.model.vcs.Subversion
import org.megrez.server.model.tasks._
import org.megrez.server.trigger.Trigger
import actors.TIMEOUT
import java.io.File
import org.neo4j.graphdb.Node
import org.megrez.server.core.TriggerBuild
import org.scalatest.{BeforeAndAfterEach, Spec, BeforeAndAfterAll}

class OnChangesTest extends Spec with ShouldMatchers with BeforeAndAfterAll with BeforeAndAfterEach with IoSupport with Neo4JSupport {
  describe("OnChanges Trigger") {
    it("should trigger build with changes") {
      val trigger = new OnChanges(pipeline, workingDir, self)
      trigger.start

      receiveWithin(1000) {
        case TriggerBuild(triggeredPipeline: Pipeline, changes: Set[Change]) =>
          triggeredPipeline should equal(pipeline)
          changes should have size (1)
          changes.head match {
            case r: Subversion.Revision =>
              r.revision should equal(42)
              r.material should equal(material)
            case _ => fail
          }
          trigger.stop
        case TIMEOUT =>
          trigger.stop
          fail
        case _ =>
          trigger.stop
          fail
      }
    }

    it("should manually trigger build with no previous changes") {
      val trigger = new OnChanges(pipeline, workingDir, self)
      trigger.startTrigger ! Trigger.ExecuteOnce

      receiveWithin(1000) {
        case TriggerBuild(triggeredPipeline: Pipeline, changes: Set[Change]) =>
          triggeredPipeline should equal(pipeline)
          changes should have size (1)
          changes.head match {
            case r: Subversion.Revision =>
              r.revision should equal(42)
              r.material should equal(material)
            case _ => fail
          }
          trigger.stop
        case TIMEOUT =>
          trigger.stop
          fail
        case _ =>
          trigger.stop
          fail
      }
    }
  }

  var material: Material = null
  var pipeline: Pipeline = null
  val workingDir = new File(System.getProperty("user.dir"))

  override def beforeEach() {
    val changeSource = ChangeSource(Map("type" -> "mock"))
    material = Material(Map("source" -> changeSource))
    val author = Job(Map("name" -> "author", "tasks" -> List()))
    val publish = Job(Map("name" -> "publish", "tasks" -> List()))
    val packageJob = Job(Map("name" -> "package", "tasks" -> List()))
    val ut = Stage(Map("name" -> "UT", "jobs" -> List(author, publish)))
    val packageStage = Stage(Map("name" -> "package", "jobs" -> List(packageJob)))
    pipeline = Pipeline(Map("name" -> "WGSN-bundles", "materials" -> List(material), "stages" -> List(ut, packageStage)))
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
      Some(Subversion.Revision(Map("revision" -> 42, "metarial" -> material)))

    def changes(workingDir: File, previous: Option[Any]) = None
  }

  object MockChangeSource extends Plugin(ChangeSource, "mock") {
    def apply(node: Node) = new MockChangeSource(node)
  }
}