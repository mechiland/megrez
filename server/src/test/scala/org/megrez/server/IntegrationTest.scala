package org.megrez.server

import _root_.main.scala.org.megrez.server.trigger.ManualTrigger
import org.scalatest.matchers.ShouldMatchers
import java.io.File
import org.scalatest.{BeforeAndAfterEach, Spec}
import trigger.Svn
import io.Source
import actors.Actor._
import actors.{TIMEOUT, Actor}


class IntegrationTest extends Spec with ShouldMatchers with BeforeAndAfterEach with AgentTestSuite {
  var trigger: ManualTrigger = _
  var scheduler: Dispatcher = _
  var agent: Agent = _

  describe("svn trigger") {

//    it("should trigger build for the first time") {
//      scheduler !? AgentConnect(agent) match {
//        case _: Success =>
//        case _ => fail
//      }
//      agent.status should be === AgentStatus.Idle
//
//      trigger !? "click" match {
//        case _: Success =>
//        case _ => fail
//      }
//
//      expectAgentGotJob
//      agent.status should be === AgentStatus.Busy
//    }
//
//    it("should not trigger build if no change") {
//      scheduler !? AgentConnect(agent) match {
//        case _: Success =>
//        case _ => fail
//      }
//
//      trigger !? "click" match {
//        case _: Success =>
//        case _ => fail
//      }
//
//      expectAgentGotJob
//      svn.needTriggerScheduler should be === true
//      agent.status should be === AgentStatus.Busy
//
//      agent !? new JobFinished(agent, "pipeline1", "stage1", "1") match {
//        case _: Success =>
//        case _ => fail
//      }
//      expectAgentFinishedJob
//      agent.status should be === AgentStatus.Idle
//      trigger !? "click" match {
//        case _: Success =>
//        case _ => fail
//      }
//
//      svn.needTriggerScheduler should be === false
//      Thread.sleep(500)
//      agent.status should be === AgentStatus.Idle
//    }
//  }

//  describe("test svn commit") {
//    it("should detected svn commit") {
//      scheduler !? AgentConnect(agent) match {
//        case _: Success =>
//        case _ => fail
//      }
//
//      trigger !? "click" match {
//        case _: Success =>
//        case _ => fail
//      }
//
//      expectAgentGotJob
//      agent.status should be === AgentStatus.Busy
//
//      agent !? new JobFinished(agent, "pipeline1", "stage1", "1") match {
//        case _: Success =>
//        case _ => fail
//      }
//      agent.status should be === AgentStatus.Idle
//      svnCommit
//
//      trigger !? "click" match {
//        case _: Success =>
//        case _ => fail
//      }
//
//      expectAgentGotJob
//
//    }
  }

  def run(tmpDir: File, cmd: String) = {
    val process: Process = Runtime.getRuntime().exec(cmd, Array[String](), tmpDir)
    val exitCode: Int = process.waitFor
    if (exitCode != 0) {
      Source.fromInputStream(process.getErrorStream).getLines().foreach {line => println(line)}
    }
  }

  def svnCommit() {
    val tmpDir = new File(System.getProperty("user.dir") + "/target/" + System.currentTimeMillis)
    tmpDir.mkdirs
    run(tmpDir, "svn co " + svnUrl + " .")
    val newFile: File = new File(tmpDir, System.currentTimeMillis + ".txt")
    newFile.createNewFile;
    run(tmpDir, "svn add " + newFile.getName)
    run(tmpDir, "svn ci -m 'added_file'")
  }

  var svnDir: String = _
  var svnUrl: String = _
  var svn: Svn = _

  override def beforeEach() {
    svnDir = System.getProperty("user.dir") + "/src/test/resources/repository/svn"
    svnUrl = "file://" + new File(svnDir).getAbsolutePath();
    val pipeline: PipelineConfig = new PipelineConfig("pipeline1", new SvnMaterial(svnUrl), List())
    svn = new Svn(pipeline)
    scheduler = new Dispatcher()
    trigger = new ManualTrigger(svn, scheduler)
    agent = new Agent(new ActorBasedAgentHandler(self), scheduler)

    agent start;
    scheduler start;
    trigger start;
  }

  override def afterEach() {
    agent ! Exit();
    scheduler ! Exit();
    trigger ! Exit();
  }

  class SpyActor(val target: Actor, val spy: Actor) extends Actor {
    def act() {
      loop {
        react {
          case msg: Any => {
            target ! msg
            spy ! msg
          }
        }
      }
    }
  }
}
