package main.scala.org.megrez.server.trigger

import org.megrez.server.TriggerMessage

trait VersionControl {

  def checkChange(): Boolean

  def getChange(): TriggerMessage

}