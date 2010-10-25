package org.megrez.agent.vcs

import org.scalatest.matchers.ShouldMatchers
import java.io.File
import collection.immutable.Map
import org.scalatest._
import java.lang.String

class SubversionTest extends Spec with ShouldMatchers with BeforeAndAfterEach {
  var properties = Map[String, Any]()
  describe("Subversion") {
    it("should check out repository") {
      subversion.checkout(root, null)
      new File(root, "README") should be('exists)
      new File(root, "REVISION_2") should be('exists)
    }
    it("should check out repository for a given revision") {
      subversion.checkout(root, 1)
      new File(root, "README") should be('exists)
      new File(root, "REVISION_2") should not be('exists)
    }
  }

  val root = new File(System.getProperty("user.dir"), "vcs_svn")
  var subversion : Subversion = _

  override def beforeEach() {
    root.mkdirs
    subversion = new Subversion("file://" + properties("agent.vcs.root") + "/svn/agent_test")
  }

  override def afterEach() {
    delete(root)
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