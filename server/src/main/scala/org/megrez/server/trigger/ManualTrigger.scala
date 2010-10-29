package main.scala.org.megrez.server.trigger

import org.megrez.server.trigger.Trigger
import actors.{Actor, Exit}
import org.megrez.server.Success

class ManualTrigger(val versionControl: VersionControl, val target: Actor) extends Actor with Trigger {
  def act {
    loop {
      react {
        case _: Exit => exit
        case "click" => {
          triggerRevision
          reply(Success())
        }
      }
    }
  }
}
