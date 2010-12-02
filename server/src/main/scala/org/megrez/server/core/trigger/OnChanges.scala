package org.megrez.server.core.trigger

import actors.Actor
import actors.Actor._
import org.megrez.server.trigger.Trigger
import org.megrez.server.model.{Material, Pipeline}
import java.io.File
import java.util.TimerTask
import org.megrez.Stop

class OnChanges(val pipeline: Pipeline, val workingDir: File, val scheduler: Actor) extends Trigger  {
  private val buildTrigger = actor {
    loop {
      react {
        case Trigger.Execute =>
          scheduler ! Pair(pipeline, pipeline.materials.map { _.getChange(workingDir).get })
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