package org.megrez.server.trigger

import main.scala.org.megrez.server.trigger.VersionControl
import org.megrez.server.{Pipeline, TriggerMessage}
import io.Source
import org.megrez.server.util.CommandUtil

class Svn(val pipeline: Pipeline) extends VersionControl {
  private var revision: String = _

  def currentRevision() = revision
  def getChange() = new TriggerMessage(pipeline.name, revision)

  def checkChange() = {
    var changed = false
    val outputs: Iterator[String] = CommandUtil.run("svn info " + pipeline.material.url)
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