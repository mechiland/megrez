package org.megrez.server.trigger

import actors.Actor
import actors.Actor._
import org.megrez.util.Logging
import java.util.TimerTask
import org.megrez.server.TriggerToScheduler
import org.megrez.Stop

class OnChanges(val materials: Materials, val scheduler: Actor, every: Long) extends Trigger with Logging {
  private val buildTrigger = actor {
    loop {
      react {
        case Trigger.Execute =>
          if (materials.hasChanges) {
            info("Changes detected, pipeline: " + materials.pipeline.name)
            scheduler ! TriggerToScheduler.TrigBuild(materials.pipeline, materials.changes)
          }
        case Trigger.ExecuteOnce =>
          if (materials.previous.values.head == None)
            materials.hasChanges
          info("manual trigger, pipeline:" + materials.pipeline.name)
          scheduler ! TriggerToScheduler.TrigBuild(materials.pipeline, materials.changes)
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
    task = Trigger.schedule(every, startTrigger)
  }

  def stop {
    if (task != null) task.cancel
    buildTrigger ! Stop
  }
}