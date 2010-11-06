package org.megrez.server

import org.scalatest.matchers.ShouldMatchers
import trigger.SvnTestRepo
import org.scalatest.{BeforeAndAfterEach, Spec}
import actors.Actor._
import actors.TIMEOUT
import org.megrez.{Job, Task}

class MegrezTest extends Spec with ShouldMatchers with BeforeAndAfterEach with SvnTestRepo with AgentTestSuite {
//  describe("App") {
//    it("should receive add pipeline event and lanunch build if pipeline has new check in") {
//
//      Megrez.pipelineManager ! new AddPipeline(pipeline)
//
//      agent ! SetResources(Set("LINUX"))
//      Megrez.dispatcher ! AgentConnect(agent)
//
//      receiveWithin(2000) {
//        case msg: String => if(msg.indexOf(job1.name) < 0) fail
//        case TIMEOUT => println("TIMEOUT"); fail
//        case msg: Any => println(msg); fail
//      }
//    }
//  }
//
//  var agent: Agent = _
//  var job1: Job = _
//  var job2: Job = _
//  var pipeline: Pipeline = _
//
//  override def beforeEach() {
//    setupSvnRepo
//    agent = new Agent(new ActorBasedAgentHandler(self), Megrez.dispatcher)
//    agent start;
//
//    job1 = new Job("linux-firefox", Set("LINUX"), List[Task]())
//    job2 = new Job("win-ie", Set("WIN"), List[Task]())
//    val stage1 = new Pipeline.Stage("ut", Set(job1, job2))
//    val stage2 = new Pipeline.Stage("ft", Set(job1, job2))
//    pipeline = new Pipeline("cruise", new SvnMaterial(svnUrl), List(stage1, stage2))
//  }
//
//  override def afterEach() {
//    Megrez.pipelineManager ! new RemovePipeline(pipeline)
//    teardownSvnRepo
//    agent ! Exit();
//    Megrez.stop
//  }
}
