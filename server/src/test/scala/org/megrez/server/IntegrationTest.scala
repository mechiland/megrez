package org.megrez.server

import org.scalatest.matchers.ShouldMatchers
import java.io.File
import org.scalatest.{BeforeAndAfterEach, Spec}
import trigger.{Trigger, Svn}

class IntegrationTest extends Spec with ShouldMatchers with BeforeAndAfterEach {
  var trigger: Trigger = _
  var scheduler: Scheduler = _
  var agent: Agent = _

  describe("svn trigger") {

    it("should trigger build for the first time") {
      scheduler !? AgentConnect(agent) match {
        case _: Success =>
        case _ => fail
      }

      trigger !? "click" match {
        case _: Success =>
        case _ => fail
      }

      Thread.sleep(500)
      agent.status should be === AgentStatus.Busy
    }

    it("should not trigger build if no change") {
      scheduler !? AgentConnect(agent) match {
        case _: Success =>
        case _ => fail
      }

      trigger !? "click" match {
        case _: Success =>
        case _ => fail
      }

      Thread.sleep(500)
      agent.status should be === AgentStatus.Busy

      agent !? new JobFinished(agent, "pipeline1", "stage1", "1") match {
        case _: Success =>
        case _ => fail
      }

      trigger !? "click" match {
        case _: Success =>
        case _ => fail
      }

      Thread.sleep(500)
      agent.status should be === AgentStatus.Idle
    }
  }

  override def beforeEach() {
    val svnDir: String = System.getProperty("user.dir") + "/src/test/resources/repository/svn"
    val svnUrl: String = "file://" + new File(svnDir).getAbsolutePath();
    val pipeline: PipelineConfig = new PipelineConfig("pipeline1", new SvnMaterial(svnUrl))
    val svn: Svn = new Svn(pipeline)
    scheduler = new Scheduler()
    trigger = new Trigger(svn, scheduler)
    agent = new Agent()

    agent start;
    scheduler start;
    trigger start;
  }

  override def afterEach() {
    agent ! Exit();
    scheduler ! Exit();
    trigger ! Exit();
  }
}