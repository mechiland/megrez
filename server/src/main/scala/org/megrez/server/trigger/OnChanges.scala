package org.megrez.server.trigger

import actors.Actor
import actors.Actor._
import org.megrez.util.Logging
import java.util.TimerTask
import org.megrez.server.{Common, TriggerToScheduler}

class OnChanges(val materials : Materials, val scheduler : Actor, every : Long) extends Trigger with Logging {
  private val buildTrigger = actor {
    loop {
      react {
        case Trigger.Execute =>
          if (materials.hasChanges) {
            info("Changes detected, pipeline: " + materials.pipeline.name)
            scheduler ! TriggerToScheduler.TrigBuild(materials.pipeline, materials.changes)
          }
        case Common.Stop => exit
        case _ =>
      }
    }
  }

  private var task : TimerTask = null

  def start {
    buildTrigger.start
    task = Trigger.schedule(every, buildTrigger)
  }

  def stop {
    if (task != null) task.cancel
    buildTrigger ! Common.Stop
  }
}