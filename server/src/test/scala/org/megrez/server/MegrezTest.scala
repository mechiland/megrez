package org.megrez.server

import org.scalatest.matchers.ShouldMatchers
import org.megrez._
import org.megrez.Pipeline.Stage
import util.JSON
import vcs.Subversion
import java.io.File
import actors.Actor._
import actors.{TIMEOUT, Actor}
import scala.io.Source
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Spec}

class MegrezTest extends Spec with ShouldMatchers with BeforeAndAfterEach with BeforeAndAfterAll with Neo4jHelper{
  val job = new Job("linux-firefox", Set(), List[Task]())
  
  describe("Core Actors") {
    it("should trig build for pipeline when pipeline first added") {      
      val pipeline = new Pipeline("pipeline", Set(new Material(new Subversion(url))), List(new Stage("name", Set(job))))

      megrez.pipelineManager ! ToPipelineManager.AddPipeline(pipeline)
      megrez.agentManager ! ToAgentManager.RemoteAgentConnected(new ActorBasedAgentHandler(self))

      receiveWithin(2000) {
        case message: String =>
          JSON.read[AgentMessage](message) match {
            case assignment: JobAssignment =>
              assignment.job.name should equal("linux-firefox")
            case _ => fail
          }
        case TIMEOUT => fail
        case _ => fail
      }
    }

    it("should trig build for pipeline when pipeline material changes") {      
      val pipeline = new Pipeline("pipeline", Set(new Material(new Subversion(url))), List(new Stage("name", Set(job))))
      val handler = new ActorBasedAgentHandler(self)

      megrez.pipelineManager ! ToPipelineManager.AddPipeline(pipeline)
      megrez.agentManager ! ToAgentManager.RemoteAgentConnected(handler)

      receiveWithin(200) {
        case message: String =>
          JSON.read[AgentMessage](message) match {
            case assignment: JobAssignment =>
              assignment.job.name should equal("linux-firefox")
            case _ => fail
          }
          handler.agent ! JobCompleted()
          checkin(checkout(url), "revision")
        case TIMEOUT => fail
        case _ => fail
      }

      receiveWithin(1000) {
        case message: String =>
        case TIMEOUT => fail
        case _ => fail
      }
    }
  }

  class ActorBasedAgentHandler(val main: Actor) extends AgentHandler {
    var agent: Actor = null

    def assignAgent(agent: Actor) {
      this.agent = agent
    }

    def send(message: String) {
      main ! message
    }
  }

  private var url = ""
  private var workingDir: File = _
  private var root: File = _
  private var megrez: Megrez = _

  override def beforeEach() {
    root = new File(System.getProperty("user.dir"), "target/vcs/svn")
    workingDir = new File(root, "work")
    val repository = new File(root, "repository")

    List(root, workingDir, repository).foreach(_ mkdirs)

    val repositoryName = String.valueOf(System.currentTimeMillis)
    val process = Runtime.getRuntime().exec("svnadmin create " + repositoryName, null, repository)
    process.waitFor match {
      case 0 =>
      case _ => fail("can't setup repository")
    }

    url = "file://" + new File(root, "repository/" + repositoryName).getAbsolutePath
    megrez = new Megrez(1000)
  }

  override def afterEach() {
    megrez.stop
    delete(root)
  }

  override def afterAll(){
    cleanupDatabase
  }


  private def delete(file: File) {
    file.listFiles.foreach {
      file =>
        if (file.isDirectory) delete(file) else file.delete
    }
    file.delete
  }

  private def run(command: String) {
    run(command, root)
  }

  private def run(command: String, workingDir: File) {
    val cmd = Runtime.getRuntime().exec(command, null, workingDir)
    cmd.waitFor match {
      case 0 =>
      case _ => fail(Source.fromInputStream(cmd.getErrorStream).mkString)
    }
  }

  private def checkin(repository: File, file: String) {
    val revision = new File(repository, file)
    revision.createNewFile

    run("svn add " + revision.getAbsolutePath)
    run("svn ci . -m \"checkin\"", repository)
  }

  private def checkout(url: String) = {
    val target = new File(root, "checkout_" + System.currentTimeMillis)
    run("svn co " + url + " " + target)
    target
  }
}
