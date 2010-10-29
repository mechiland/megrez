package org.megrez.server.trigger

import main.scala.org.megrez.server.trigger.VersionControl
import actors.Actor
import org.megrez.server.TriggerMessage

trait Trigger {
  val versionControl: VersionControl
  val target: Actor

  def triggerRevision = {
    versionControl.checkChange()
    val buildChange: TriggerMessage = versionControl.getChange
    if (buildChange != null) {
      target ! buildChange
    }
  }
}




