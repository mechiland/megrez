package org.megrez

import org.scalatest.matchers.ShouldMatchers

import org.scalatest.{BeforeAndAfterEach, Spec}
import actors.TIMEOUT
import actors.Actor._
import java.net.{URLEncoder, URL}
import java.io.{DataOutputStream, File}
import io.Source

class SmokeTest extends Spec with ShouldMatchers with BeforeAndAfterEach {

  describe("Smoke test") {
    it("should assign job to agent when source code changes") {
      startServer()
      startAgent()
      startAgent()

      receiveWithin(1000) {
        case TIMEOUT =>
        case _ => fail
      }

      val json = """{"name":"pipeline","materials":[{"type":"svn","url":""" + '"' + url + '"' + ""","dest":"$main"}],"stages":[{"name":"stage","jobs":[{"name":"job","resources":[],"tasks":[{"type":"cmd","command":"ls"}]}]}]}"""

      addPipeline(json)
      
      receiveWithin(5000) {
        case TIMEOUT =>
        case _ => fail
      }
    }
  }

  val Client = org.megrez.agent.Main
  val Server = org.megrez.server.Main

  private def startAgent() {
    Client.start("ws://localhost:8080/agent", agentWorkingDir)
  }

  private def startServer() {
    Server.start(8080)
  }

  def addPipeline(json: String): Unit = {
    val url = new URL("http://localhost:8080/pipelines")
    val connection = url.openConnection
    connection.setDoInput(true)
    connection.setDoOutput(true)
    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
    val out = new DataOutputStream(connection.getOutputStream)
    out.writeBytes("pipeline=" + URLEncoder.encode(json, "UTF-8"))
    out.flush
    out.close
    
    println(Source.fromInputStream(connection.getInputStream).mkString)
  }

  var root: File = _
  var agentWorkingDir: File = _
  var url = ""

  override def beforeEach() {
    root = new File(System.getProperty("user.dir"), "target/functional-test")
    agentWorkingDir = new File(root, "agent")
    val repository = new File(root, "repository")
    List(root, agentWorkingDir, repository).foreach(_ mkdirs)

    val repositoryName = String.valueOf(System.currentTimeMillis)
    val process = Runtime.getRuntime().exec("svnadmin create " + repositoryName, null, repository)
    process.waitFor match {
      case 0 =>
      case _ => fail("can't setup repository")
    }

    url = "file://" + new File(root, "repository/" + repositoryName).getAbsolutePath
    checkin(checkout(url), "revision_1")
  }  

  override def afterEach() {
    delete(root)
  }

  private def delete(file: File) {
    file.listFiles.foreach {
      file =>
        if (file.isDirectory) delete(file) else file.delete
    }
    file.delete
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

  private def run(command: String) {
    run(command, root)
  }

  private def run(command: String, workingDir : File) {
    val cmd = Runtime.getRuntime().exec(command, null, workingDir)
    cmd.waitFor match {
      case 0 =>
      case _ => fail(Source.fromInputStream(cmd.getErrorStream).mkString)
    }
  }  
}