package org.megrez.agent

import org.scalatest.matchers.ShouldMatchers
import java.io.File
import java.lang.String
import org.scalatest._
import org.jboss.netty.handler.codec.http.websocket.DefaultWebSocketFrame
import scala.actors._
import scala.actors.Actor._
import java.net.URI

class IntegrationTest extends ServerIntegration with ShouldMatchers {
  describe("Version control intergration") {
    it("should handle subversion job assignment") {
      val subversion = "file://" + properties("agent.vcs.root") + "/svn/agent_test"

      MegrezHandshake.actor = self
      ReceiveResponse.actor = self

      server.response(WebSocketHandshake, MegrezHandshake, ReceiveResponse)
      server.start
            
      val worker = new Worker(new FileWorkspace(new File(root.getAbsolutePath)))
      worker.start
      serverConnection = new Server(new URI("ws://localhost:8080/"), 5000, worker)
      serverConnection.connect
      
      val jobAssignment =
                 """{"pipeline" : {"id" : "pipeline", "vcs" : {"type" : "svn", "url" : """ + '"' + subversion + '"' + """}},
                     "workSet"  : {"revision" : "2"},
                     "job"      : {"tasks" : [] } }"""

      receiveWithin(1000) {
        case "MEGREZ HANDSHAKE" => server.send(new DefaultWebSocketFrame(jobAssignment))
        case TIMEOUT => fail
        case _ => fail
      }

      receiveWithin(2000) {
        case """{"status" : "completed"}""" => server.send(new DefaultWebSocketFrame(jobAssignment))
        case TIMEOUT => fail
        case _ => fail
      }

      new File(root, "pipeline/README") should be('exists)
      new File(root, "pipeline/REVISION_2") should be('exists)
    }
  }

  val root = new File(System.getProperty("user.dir"), "integration")
  var properties = Map[String, Any]()
  var serverConnection : Server = _

  override def beforeEach() {
    super.beforeEach
    root.mkdirs
  }

  override def afterEach() {
    super.afterEach
    delete(root)
    MegrezHandshake.actor = null
    ReceiveResponse.actor = null
    serverConnection.shutdown
  }

  def delete(file : File) {
    file.listFiles.foreach {file =>
      if (file.isDirectory) delete(file) else file.delete
    }
    file.delete
  }

  protected override def runTests(testName: Option[String], reporter: Reporter, stopper: Stopper, filter: Filter, configMap: Map[String, Any], distributor: Option[Distributor], tracker: Tracker) {
    properties = configMap
    super.runTests(testName, reporter, stopper, filter, configMap, distributor, tracker)
  }
}