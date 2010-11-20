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
        case Some(everyTime: Long) => if (everyTime < 0) {
          materials.hasChanges
          info("manual trigger, pipeline:" + materials.pipeline.name)
          scheduler ! TriggerToScheduler.TrigBuild(materials.pipeline, materials.changes)
        }
        case Stop => exit
        case _ =>
      }
    }
  }

  private var task: TimerTask = null

  def start {
    buildTrigger.start
    if (every >= 0)
      task = Trigger.schedule(every, buildTrigger)
    else {
      buildTrigger ! Some(every)
    }
  }

  def stop {
    if (task != null) task.cancel
    buildTrigger ! Stop
  }
}