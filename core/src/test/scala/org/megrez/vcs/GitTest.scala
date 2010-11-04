package org.megrez.vcs

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{Spec, BeforeAndAfterEach}
import org.megrez.runtime.ShellCommand
import java.io.File
import io.Source

class GitTest extends Spec with ShouldMatchers with BeforeAndAfterEach with ShellCommand {
  describe("Subversion") {
    it("should detect change if working dir empty") {
      val git = new Git(repositoryURL)
      git.checkout(workingDir, None)
      makeNewCommit(repositoryURL)
      git.changes(workingDir) match {
        case Some(revision: String) =>
        case _ => fail
      }
    }

    it("should return false if given dir not a directory") {
      val git = new Git(repositoryURL)
      makeNewCommit(repositoryURL)
      git.changes(workingDir) match {
        case Some(revision: String) =>
        case _ => fail
      }
    }
    it("should check out specified commit") {
      val git = new Git(repositoryURL)
      makeNewCommit(repositoryURL)
      val changes: Option[Any] = git.changes(workingDir)
      git.update(workingDir, changes)
      println(Source.fromInputStream(run("git log --pretty=%H -1", workingDir).getInputStream).mkString)
    }

    it("should return true if given dir is a directory") {
      val git = new Git(repositoryURL)
      makeNewCommit(repositoryURL)
      git.checkout(workingDir, None)
      git.isRepository(workingDir) should equal(true)
    }
  }

  private var root: File = _
  private var workingDir: File = _
  private var repositoryURL: String = _

  override protected def beforeEach() {
    root = new File(System.getProperty("user.dir"), "target/vcs/git")
    workingDir = new File(root, "work")
    val repository = new File(root, "repository")
    List(root, workingDir, repository).foreach(_ mkdirs)

    run("git init ", repository)

    repositoryURL = repository.getAbsolutePath
  }

  override def afterEach() {
    delete(root)
  }

  private def makeNewCommit(repoDir: String) {
    new File(repoDir, String.valueOf(System.currentTimeMillis) + ".txt").createNewFile
    run("git add --all")
    run("git commit -m 'added_new_file'")
  }

  private def delete(file: File) {
    file.listFiles.foreach {
      file =>
        if (file.isDirectory) delete(file) else file.delete
    }
    file.delete
  }
}