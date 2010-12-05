package org.megrez.server.core.trigger

import actors.Actor
import actors.Actor._
import org.megrez.server.trigger.Trigger
import java.io.File
import java.util.TimerTask
import org.megrez.Stop
import org.megrez.server.core.TriggerBuild
import org.megrez.server.model.Pipeline

class OnChanges(val pipeline: Pipeline, val workingDir: File, val scheduler: Actor) extends Trigger {
  private val buildTrigger = actor {
    loop {
      react {
        case Trigger.Execute =>
          scheduler ! TriggerBuild(pipeline, pipeline.materials.map {_.getChange(workingDir).get})
        case Trigger.ExecuteOnce =>
          scheduler ! TriggerBuild(pipeline, pipeline.materials.map {
            material => Option(material.lastChange()).orElse(material.getChange(workingDir)).get
          })
        case Stop => exit
        case _ =>
      }
    }
  }

  private var task: TimerTask = null

  def startTrigger: Actor = {
    buildTrigger.start
    return buildTrigger
  }

  def start {
    task = Trigger.schedule(500, startTrigger)
  }

  def stop {
    if (task != null) task.cancel
    buildTrigger ! Stop
  }
}