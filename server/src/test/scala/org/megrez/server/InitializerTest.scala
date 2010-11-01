package org.megrez.server

import org.scalatest.matchers.ShouldMatchers
import trigger.SvnTestRepo
import org.scalatest.{BeforeAndAfterEach, Spec}
import actors.Actor._

class InitializerTest extends Spec with ShouldMatchers with BeforeAndAfterEach with SvnTestRepo with AgentTestSuite {
  describe("App") {
    it("should receive add pipeline event and lanunch build if pipeline has new check in") {

      Initializer.pipelineManager ! new AddPipeline(pipeline)

      Initializer.dispatcher !? AgentConnect(agent) match {
        case _: Success =>
        case msg: Any => println(msg); fail
      }

      expectAgentGotJob
    }
  }

  var agent: Agent = _
  var pipeline: Pipeline = _

  override def beforeEach() {
    setupSvnRepo
    Initializer.initializeApp
    agent = new Agent(new ActorBasedAgentHandler(self), Initializer.dispatcher)
    agent start;

    val job1 = new Job("linux-firefox", Set(), List(new Task()))
    val job2 = new Job("win-ie", Set(), List(new Task()))
    val stage1 = new Pipeline.Stage("ut", Set(job1, job2))
    val stage2 = new Pipeline.Stage("ft", Set(job1, job2))
    pipeline = new Pipeline("cruise", new SvnMaterial(svnUrl), List(stage1, stage2))
  }

  override def afterEach() {
    teardownSvnRepo
    agent ! Exit();
    Initializer.stopApp
  }
}
