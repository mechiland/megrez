package org.megrez.agent.vcs

import org.scalatest.matchers.ShouldMatchers
import java.io.File
import collection.immutable.Map
import org.scalatest._
import java.lang.String

class SubversionTest extends Spec with ShouldMatchers with BeforeAndAfterEach {
  var properties = Map[String, Any]()
  describe("Subversion checkout") {
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

  describe("Subversion update") {
    it("should update to given revision") {
      subversion.checkout(root, 1)
      new File(root, "REVISION_2") should not be('exists)
      subversion.update(root, 2)
      new File(root, "README") should be('exists)
      new File(root, "REVISION_2") should be('exists)      
    }
  }

  describe("Subversion working dir check") {
    it("should return true if working dir is a subversion repository") {
      subversion.checkout(root, null)
      subversion.isRepository(root) should be(true)
    }

    it("should return false if working dir is not a subversion repository") {
      subversion.isRepository(root) should be(false)
    }
  }

  describe("Subversion exception") {
    it("should catch all output when checkout and exception occurs") {
      new File(root, "README").createNewFile
      val exception : Throwable = evaluating { subversion.checkout(root, null) } should produce [VersionControlException]
      exception.getMessage should startWith("svn: Failed to add file")
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