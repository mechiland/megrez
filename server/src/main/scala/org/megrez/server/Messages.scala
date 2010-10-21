package org.megrez.server

import actors._
import scala.actors.Actor._

abstract class CommonMessage
case class Success() extends CommonMessage
case class Exit() extends CommonMessage

abstract class AgentMessage
case class SetTags(val tags : Set[String]) extends AgentMessage

abstract class JobMessage
case class Job() extends JobMessage
case class JobConfirm(val agent : Agent) extends JobMessage
case class JobReject(val agent : Agent) extends JobMessage
