package org.megrez.server.trigger

import main.scala.org.megrez.server.trigger.VersionControl
import actors.Actor
import org.megrez.server.{Exit, Success, Scheduler, TriggerMessage}

class Trigger(val versionControl: VersionControl, val target: Actor) extends Actor {
  def act {
    loop {
      react {
        case _: Exit => exit
        case _ => {
          versionControl.checkChange()
          val buildChange: TriggerMessage = versionControl.getChange
          if (buildChange != null) {
            target ! buildChange
          }
          reply(Success())
        }
      }
    }
  }
}




