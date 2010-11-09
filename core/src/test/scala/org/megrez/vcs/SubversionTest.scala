package org.megrez.vcs

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import java.io.File
import java.lang.String
import io.Source

class SubversionTest extends Spec with ShouldMatchers with BeforeAndAfterEach {
  describe("Subversion") {
    it("should detect change") {
      val subversion = new Subversion(url)
      subversion.changes(workingDir, None) match {
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

    it("should checkout repository head") {
      val repository = checkout(url)
      checkin(repository, "REVISION_1")

      val subversion = new Subversion(url)
      subversion.checkout(workingDir, None)
      subversion.isRepository(workingDir) should equal(true)
      new File(workingDir, "REVISION_1") should be('exists)
    }

    it("should checkout particluar revision") {
      val repository = checkout(url)
      checkin(repository, "REVISION_1")

      val subversion = new Subversion(url)
      subversion.checkout(workingDir, Some(0))
      new File(workingDir, "REVISION_1") should not be ('exists)
    }

    it("should update to head") {
      val repository = checkout(url)

      val subversion = new Subversion(url)
      subversion.checkout(workingDir, None)

      checkin(repository, "REVISION_1")

      subversion.update(workingDir, None)
      new File(workingDir, "REVISION_1") should be ('exists)
    }

    it("should update to particular revision") {
      val repository = checkout(url)
      checkin(repository, "REVISION_1")

      val subversion = new Subversion(url)
      subversion.checkout(workingDir, None)

      checkin(repository, "REVISION_2")

      subversion.update(workingDir, Some(2))
      new File(workingDir, "REVISION_2") should be ('exists)
    }
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
}