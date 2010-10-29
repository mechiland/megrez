package org.megrez.server.trigger

import actors.Actor
import main.scala.org.megrez.server.trigger.VersionControl
import java.util.{TimerTask, Timer}

class AutoTrigger(val versionControl: VersionControl, val target: Actor) extends Trigger{
  val timer: Timer = new Timer()
  timer.schedule(new TriggerTimerTask(this),1000,2*1000)
}
class TriggerTimerTask(val trigger:Trigger) extends TimerTask {
  def run() = {trigger.triggerRevision}
}
