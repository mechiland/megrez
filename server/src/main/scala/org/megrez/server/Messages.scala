package org.megrez.server

abstract class CommonMessage
case class Success() extends CommonMessage
case class Exit() extends CommonMessage

abstract class AgentMessage
case class SetResources(val resources : Set[String]) extends AgentMessage

abstract class JobMessage
case class JobRequest(val job : Job) extends JobMessage
case class JobConfirm(val agent : Agent) extends JobMessage
case class JobReject(val agent : Agent) extends JobMessage
