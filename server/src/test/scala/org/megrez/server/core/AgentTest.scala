package org.megrez.server.core

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfterAll, Spec}
import org.megrez.server.model._
import data.Graph
import tasks.{CommandLine, Ant}
import vcs.Subversion
import org.scalatest.mock.MockitoSugar
import scala.actors.Actor._
import org.megrez.server.{AgentToDispatcher, IoSupport, Neo4JSupport}
import actors.TIMEOUT
import org.mockito.Mockito._

class AgentTest extends Spec with ShouldMatchers with BeforeAndAfterAll with IoSupport with Neo4JSupport with MockitoSugar {
  describe("Agent") {
    it("should confirm job and send job assignment") {
      val handler = mock[AgentHandler]
      val agentActor = new org.megrez.server.core.Agent(agent, handler, self)
      agentActor ! (build, build.next.head)
      receiveWithin(1000) {
        case AgentToDispatcher.Confirm =>
          verify(handler).send("""{"materials":[],"pipeline":"WGSN-bundles","tasks":[],"type":"assignment","buildId":11}""")
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
  var agent: org.megrez.server.model.Agent = null
  var build: Build = null

  override def beforeAll() {
    Neo4J.start
    Graph.of(neo).consistOf(Task, Job, Stage, ChangeSource, Material, Pipeline, Build, StageExecution, JobExecution)
    Graph.consistOf(CommandLine, Ant, Subversion, Subversion.Revision, Agent)

    val changeSource = ChangeSource(Map("type" -> "svn", "url" -> "svn_url"))
    material = Material(Map("source" -> changeSource))
    author = Job(Map("name" -> "author", "tasks" -> List()))
    publish = Job(Map("name" -> "publish", "tasks" -> List()))
    packageJob = Job(Map("name" -> "package", "tasks" -> List()))
    val ut = Stage(Map("name" -> "UT", "jobs" -> List(author, publish)))
    val packageStage = Stage(Map("name" -> "package", "jobs" -> List(packageJob)))
    pipeline = Pipeline(Map("name" -> "WGSN-bundles", "materials" -> List(material), "stages" -> List(ut, packageStage)))

    build = Build(pipeline, Set())

    agent = Agent(Map("resources" -> List("WINDOWS")))
  }

  override def afterAll() {
    Neo4J.shutdown
  }

}