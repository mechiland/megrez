package org.megrez.server.core.trigger

import actors.Actor
import org.megrez.util.Logging
import java.util.{Timer, TimerTask}

trait Trigger {
  def start

  def stop

  def startTrigger: Actor
}

object Trigger extends Logging {
  object Execute
  object ExecuteOnce

  private val timer = new Timer

  def schedule(every: Long, actor: Actor) = {
    info("task scheduled for every " + every)
    val task = new TimerTask {
      def run {
        actor ! Execute
      }
    }

    timer.schedule(task, 0, every)
    task
  }
}