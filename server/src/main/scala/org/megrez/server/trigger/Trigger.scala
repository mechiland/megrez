package org.megrez.server.trigger

import main.scala.org.megrez.server.trigger.VersionControl
import actors.Actor
import org.megrez.server.{Scheduler, TriggerMessage}

class Trigger(val versionControl: VersionControl, val target: Actor) extends Actor {
  def act {
    while (true) {
      reactWithin(1000) {
        case _ => {
          versionControl.checkChange()
          val buildChange: TriggerMessage = versionControl.getChange
          if (buildChange != null) {
            target ! buildChange
          }
        }
      }
    }
  }
}




