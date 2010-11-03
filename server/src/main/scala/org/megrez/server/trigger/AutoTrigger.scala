package org.megrez.server.trigger

import actors.Actor
import java.util.{TimerTask, Timer}
import org.megrez.server.{Trigger, Pipeline}

class AutoTrigger(val pipeline: Pipeline, val target: Actor) extends Trigger {
  val timer: Timer = new Timer()
  val delay = 1000
  val period = 2 * 1000

  def start() {
    timer.schedule(new TriggerTimerTask(this), delay, period)
  }

  def stop() {
    timer.cancel();
  }
}

class TriggerTimerTask(val trigger: Trigger) extends TimerTask {
  def run() {
    trigger.checkAndTrigger
  }
}
