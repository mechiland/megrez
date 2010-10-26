package main.scala.org.megrez.server.trigger

import org.megrez.server.TriggerMessage

trait VersionControl {
  var needTriggerScheduler: Boolean = false

  def getChange(): TriggerMessage

  def checkChange()

}