package org.megrez.server

import actors._
import scala.actors.Actor._

abstract class Message
case class Job() extends Message
case class JobConfirm(val agent : Agent) extends Message
case class JobReject(val agent : Agent) extends Message
case class Exit() extends Message