package org.megrez.server.trigger

import java.io.File
import org.megrez.server.util.{Utils, EnvUtil, CommandUtil}

trait GitTestRepo {
  protected var repoDir: File = _
  protected var gitUrl: String = _

  def setupGitRepo() {
    repoDir = new File(EnvUtil.tempDir(), Utils.aRandomName)
    repoDir.mkdirs
    run(String.format("git init ."))
    makeNewCommit
    gitUrl = repoDir.getAbsolutePath
  }

  def teardownGitRepo() {
    if (EnvUtil.isWindows) {
      CommandUtil.run("rd /s /q " + repoDir.getAbsolutePath)
    } else {
      CommandUtil.run("rm -rf " + repoDir.getAbsolutePath)
    }
  }

  def makeNewCommit() {
    new File(repoDir, Utils.aRandomName + ".txt").createNewFile
    run("git add --all")
    run("git commit -m 'added_new_file'").mkString
  }

  def currentRevision() = {
    run("git log --pretty=%H -1").mkString
  }

  private def run(command: String) = {
    CommandUtil.run(command, repoDir)
  }
}