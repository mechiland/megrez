package org.megrez.server

import org.scalatest.matchers.ShouldMatchers
import trigger.SvnTestRepo
import org.scalatest.{BeforeAndAfterEach, Spec}
import actors.Actor._

class InitializerTest extends Spec with ShouldMatchers with BeforeAndAfterEach with SvnTestRepo with AgentTestSuite {
  describe("App") {
    it("should receive add pipeline event and lanunch build if pipeline has new check in") {

      Initializer.pipelineManager ! new AddPipeline(pipeline)

      agent ! SetResources(Set("LINUX"))
      Initializer.dispatcher ! AgentConnect(agent)

      expectAgentGotJob(job1)
    }
  }

  var agent: Agent = _
  var job1: Job = _
  var job2: Job = _
  var pipeline: Pipeline = _

  override def beforeEach() {
    setupSvnRepo
    agent = new Agent(new ActorBasedAgentHandler(self), Initializer.dispatcher)
    agent start;

    job1 = new Job("linux-firefox", Set("LINUX"), List(new Task()))
    job2 = new Job("win-ie", Set("WIN"), List(new Task()))
    val stage1 = new Pipeline.Stage("ut", Set(job1, job2))
    val stage2 = new Pipeline.Stage("ft", Set(job1, job2))
    pipeline = new Pipeline("cruise", new SvnMaterial(svnUrl), List(stage1, stage2))
  }

  override def afterEach() {
    Initializer.pipelineManager ! new RemovePipeline(pipeline)
    teardownSvnRepo
    agent ! Exit();
    Initializer.stopApp
  }
}
