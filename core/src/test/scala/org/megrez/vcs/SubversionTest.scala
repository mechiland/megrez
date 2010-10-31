package org.megrez.vcs

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import java.io.File

class SubversionTest extends Spec with ShouldMatchers with BeforeAndAfterEach {
  describe("Subversion") {
    it("should detect change") {
      val subversion = new Subversion(url)
      subversion.changes(workingDir) match {
        case Some(revision: Int) =>
          revision should equal(0)
        case None => fail("none")
      }
    }

    it("should return false if given dir is not a repository") {
      val subversion = new Subversion(url)
      subversion.isRepository(workingDir) should equal(false)
    }

    it("should return true if given dir is a repository") {
      val subversion = new Subversion(url)
      Runtime.getRuntime().exec("svn co " + url + " " + workingDir.getAbsolutePath).waitFor
      subversion.isRepository(workingDir) should equal(true)
    }
  }

  private var url = ""
  private var workingDir: File = _

  override def beforeEach() {
    val repositoryDir = new File(System.getProperty("user.dir"), "target/vcs/svn")
    repositoryDir.mkdirs
    val repositoryName = String.valueOf(System.currentTimeMillis)
    val process = Runtime.getRuntime().exec("svnadmin create " + repositoryName, null, repositoryDir)
    process.waitFor match {
      case 0 =>
      case 1 => fail("can't setup repository")
    }

    url = "file://" + new File(repositoryDir, repositoryName).getAbsolutePath
    workingDir = new File(repositoryDir, "work")
  }

  override def afterEach() {
    delete(new File(System.getProperty("user.dir"), "target/vcs/svn"))
  }

  private def delete(file: File) {
    file.listFiles.foreach {
      file =>
        if (file.isDirectory) delete(file) else file.delete
    }
    file.delete
  }
}