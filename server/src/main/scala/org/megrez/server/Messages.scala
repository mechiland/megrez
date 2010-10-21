package org.megrez.server

import actors._
import scala.actors.Actor._

abstract class Message
case class Job(val scheduler : Actor) extends Message
case class AgentStateChange(val agent : Actor, val state : String) extends Message
case class AgentBusy(val agent: Actor) extends Message