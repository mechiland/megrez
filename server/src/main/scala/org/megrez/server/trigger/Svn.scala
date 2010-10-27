package org.megrez.server.trigger

import main.scala.org.megrez.server.trigger.VersionControl
import org.megrez.server.{PipelineConfig, TriggerMessage, Pipeline}

class Svn(val pipeline: PipelineConfig) extends VersionControl {
  var revision: String = null

  def checkChange() = {
    val process: Process = Runtime.getRuntime().exec("svn info " + pipeline.material.url)
    val answers: Iterator[String] = scala.io.Source.fromInputStream(process.getInputStream).getLines()
    answers.foreach {
      item =>
        if (item.contains("Revision"))
          {
            val latestVersion: String = item.split(":")(1).trim
            if (latestVersion != revision) {
              revision = latestVersion
              needTriggerScheduler = true
            }
          }
    }
    process.waitFor()
  }

  def getChange(): TriggerMessage = {
    if (needTriggerScheduler == true)
      return new TriggerMessage(pipeline.name, revision)
    return null
  }
}