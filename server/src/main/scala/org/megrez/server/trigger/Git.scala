package org.megrez.server.trigger

import java.lang.String
import java.io.File
import main.scala.org.megrez.server.trigger.VersionControl
import org.megrez.server.util.CommandUtil
import org.megrez.server.{GitMaterial, TriggerMessage, Pipeline}

class Git(val pipeline: Pipeline) extends VersionControl {
  private val gitUrl: String = pipeline.material.asInstanceOf[GitMaterial].url
  private var revision: String = _

  def currentRevision = revision
  def getChange() = new TriggerMessage(pipeline.name, revision)

  def checkChange() = {
    var changed = false

    val workingDir: File = pipeline.workingDir()
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
      CommandUtil.run(String.format("git clone %s .", gitUrl), workingDir)
    }
  }
}