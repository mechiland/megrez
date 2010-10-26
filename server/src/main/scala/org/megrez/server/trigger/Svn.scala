package org.megrez.server.trigger

import main.scala.org.megrez.server.trigger.VersionControl
import org.megrez.server.{TriggerMessage, Pipeline}

class Svn(val pipeline: Pipeline) extends VersionControl {
  var revision: String = pipeline.buildRevision

  def checkChange() = {
    val process: Process = Runtime.getRuntime().exec("svn info " + pipeline.repositoryUrl)
    val answers: Iterator[String] = scala.io.Source.fromInputStream(process.getInputStream).getLines()
    answers.foreach {
      item =>
        if (item.contains("Revision"))
          {
            val latestVersion: String = item.split(":")(1).trim
            if (latestVersion.toInt > revision.toInt) {
              revision = latestVersion
              needTriggerScheduler = true
            }
          }
    }
    process.waitFor()
  }

  def getChange(): TriggerMessage = {
    if (needTriggerScheduler == true)
      return new TriggerMessage(pipeline.name,revision)
    return null
  }
}