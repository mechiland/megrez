package org.megrez.server.trigger

import main.scala.org.megrez.server.trigger.VersionControl
import actors.Actor
import org.megrez.server.TriggerMessage

trait Trigger {
  val versionControl: VersionControl
  val target: Actor

  def triggerRevision = {
    val changed = versionControl.checkChange()
    if (changed) {
      target ! versionControl.getChange
    }
  }
}




