package org.megrez.server.trigger

import actors.Actor
import java.util.{TimerTask, Timer}

trait Trigger {
  def start

  def stop
}

object Trigger {
  object Execute

  private val timer = new Timer

  def schedule(every: Long, actor: Actor) = {
    val task = new TimerTask {
      def run {
        actor ! Execute
      }
    }

    timer.schedule(task, 0, every)
    task
  }
}












