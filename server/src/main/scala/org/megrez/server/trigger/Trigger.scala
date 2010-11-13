package org.megrez.server.trigger

import actors.Actor
import java.util.{TimerTask, Timer}
import org.megrez.util.Logging

trait Trigger {
  def start

  def stop
}

object Trigger extends Logging {
  object Execute

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












