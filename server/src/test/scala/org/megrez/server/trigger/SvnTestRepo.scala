package org.megrez.server.trigger

import org.scalatest.BeforeAndAfterEach
import java.io.File
import org.megrez.server.util.{Utils, EnvUtil, CommandUtil}

trait SvnTestRepo {
  protected var repoDir: File = _
  protected var svnUrl: String = _
  
  def setupSvnRepo() {
    val tmpDir: File = EnvUtil.tempDir()
    tmpDir.mkdirs
    val repoName = Utils.aRandomName
    CommandUtil.run("svnadmin create " + repoName, tmpDir)
    repoDir = new File(tmpDir, repoName)
    svnUrl = "file://" + new File(tmpDir, repoName).getAbsolutePath
  }

  def teardownSvnRepo() {
    if (EnvUtil.isWindows) {
      CommandUtil.run("rd /s /q " + repoDir.getAbsolutePath)
    } else {
      CommandUtil.run("rm -rf " + repoDir.getAbsolutePath)
    }
  }

  def makeNewCommit() {
    CommandUtil.run(String.format("svn mkdir %s/%s -m '%s'", svnUrl, Utils.aRandomName, "create_new_dir"))
  }
}