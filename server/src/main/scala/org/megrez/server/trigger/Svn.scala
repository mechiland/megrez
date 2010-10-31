package org.megrez.server.trigger

import main.scala.org.megrez.server.trigger.VersionControl
import org.megrez.server.util.CommandUtil
import org.megrez.server.{SvnMaterial, Pipeline, TriggerMessage}

class Svn(val url: String) extends VersionControl {
  private var revision: String = _

  def currentRevision() = revision

  def checkChange() = {
    var changed = false
    val outputs: Iterator[String] = CommandUtil.run("svn info " + url)
    outputs.foreach {
      line =>
        if (line.contains("Revision")) {
          val latestVersion: String = line.split(":")(1).trim
          if (latestVersion != revision) {
            revision = latestVersion
            changed = true
          }
        }
    }
    changed
  }
}