package org.megrez

import org.scalatest.matchers.ShouldMatchers

import org.scalatest.{BeforeAndAfterEach, Spec}
import actors.TIMEOUT
import actors.Actor._
import org.apache.http.client.methods.HttpPost
import org.apache.http.message.BasicNameValuePair
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.protocol.BasicHttpContext
import java.net.{URLEncoder, URL}
import java.io.{DataInputStream, DataOutputStream, File}
import io.Source

class SmokeTest extends Spec with ShouldMatchers with BeforeAndAfterEach {
  describe("Smoke test") {
    it("should assign job to agent when source code changes") {
      startServer()
      startAgent()
      startAgent()

      receiveWithin(5000) {
        case TIMEOUT =>
        case _ => fail
      }

      val json = """{"name":"pipeline","materials":[{"type":"svn","url":"svn_url","dest":"dest"}],"stages":[{"name":"stage","jobs":[{"name":"job","resources":["LINUX"],"tasks":[{"type":"cmd","command":"ls"}]}]}]}"""

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

  var root: File = _
  var agentWorkingDir: File = _

  override def beforeEach() {
    root = new File(System.getProperty("user.dir"), "target/functional-test")
    agentWorkingDir = new File(root, "agent")
  }
}