package org.megrez.server.trigger

import java.lang.String
import java.io.File
import main.scala.org.megrez.server.trigger.VersionControl
import org.megrez.server.util.CommandUtil
class Git(val url: String, val workingDir: File) extends VersionControl {
  private var revision: String = _

  def currentRevision = revision

  def checkChange() = {
    var changed = false

    checkWorkDirectory(workingDir)

    CommandUtil.run("git pull", workingDir)

    val latestRevision = CommandUtil.run("git log --pretty=%H -1", workingDir).mkString

    if (revision != latestRevision) {
      revision = latestRevision
      changed = true
    }

    changed
  }

  private def checkWorkDirectory(workingDir: File) {
    if (!workingDir.exists) {
      workingDir.mkdirs
    }
    if (!new File(workingDir + "/.git").exists) {
      CommandUtil.run(String.format("git clone %s .", url), workingDir)
    }
  }
}