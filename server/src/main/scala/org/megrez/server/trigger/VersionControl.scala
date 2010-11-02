package main.scala.org.megrez.server.trigger

trait VersionControl {
  def checkChange(): Boolean

}