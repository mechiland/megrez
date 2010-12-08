package org.megrez.agent

import org.scalatest.matchers.ShouldMatchers
import java.io.File
import java.lang.String
import org.jboss.netty.handler.codec.http.websocket.DefaultWebSocketFrame
import scala.actors._
import scala.actors.Actor._
import java.net.URI
import io.Source
import org.megrez.{ArtifactStream, ConsoleOutput, AgentMessage, JobCompleted}
import org.megrez.util.JSON
import org.megrez.util.FileWorkspace

class IntegrationTest extends ServerIntegration with ShouldMatchers {
  describe("Version control intergration") {
    it("should handle subversion job assignment") {
      MegrezHandshake.actor = self
      ReceiveResponse.actor = self

      server.response(WebSocketHandshake, MegrezHandshake, ReceiveResponse, ReceiveResponse)
      server.start

      val worker = new Worker(new FileWorkspace(new File(root.getAbsolutePath)))
      worker.start
      serverConnection = new Server(new URI("ws://localhost:8080/"), 5000, worker)
      serverConnection.connect

      val jobAssignment =
      """{"type" : "assignment", "buildId":1, "pipeline" : "pipeline", "materials" : [{ "material" : {"type" : "svn", "url" : """ + '"' + url + '"' + """, "dest" : "$main"}, "workset" : {"revision" : 2} }], "tasks" : [{ "type" : "cmd", "command": "echo HELLO"}] }"""

      receiveWithin(1000) {
        case "MEGREZ HANDSHAKE" => server.send(new DefaultWebSocketFrame(jobAssignment))
        case TIMEOUT => fail
        case _ => fail
      }

      receiveWithin(2000) {
        case message: String =>
          JSON.read[AgentMessage](message) match {
            case ConsoleOutput(output) =>
              output should equal("HELLO")
            case _ => fail
          }
        case ArtifactStream(content) =>
        case TIMEOUT => fail
        case _ => fail
      }

      receiveWithin(2000) {
        case message: String =>
          if(!message.isEmpty)
          JSON.read[AgentMessage](message) match {
            case _: JobCompleted =>
            case _ =>
          }
        case TIMEOUT => fail
        case _ => fail
      }

      new File(root, "pipeline/README") should be('exists)
      new File(root, "pipeline/REVISION_2") should be('exists)
    }
  }

  var properties = Map[String, Any]()
  var serverConnection: Server = _

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

  private var url = ""
  private var workingDir: File = _
  private var root: File = _

  override def beforeEach() {
    super.beforeEach
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
    val svn = checkout(url)
    checkin(svn, "README")
    checkin(svn, "REVISION_2")
  }

  override def afterEach() {
    super.afterEach
    delete(root)
    MegrezHandshake.actor = null
    ReceiveResponse.actor = null
    serverConnection.shutdown
  }

  def delete(file: File) {
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
}