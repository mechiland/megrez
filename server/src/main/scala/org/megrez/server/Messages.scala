package org.megrez.server

import actors._

abstract class CommonMessage
case class Success() extends CommonMessage
case class Exit() extends CommonMessage

abstract class AgentMessage
case class SetResources(val resources : Set[String]) extends AgentMessage
case class NewAgentConnected(val handler : AgentHandler, val link : Actor) extends AgentMessage

case class TriggerMessage(val pipelineName : String, val revision : String)
case class AgentConnect(val agent : Actor)

abstract class JobMessage
case class JobRequest(val pipeline: String, val stage: String, val job : Job) extends JobMessage
case class JobConfirm(val agent : Agent, val job: Job) extends JobMessage
case class JobReject(val agent : Agent) extends JobMessage
case class JobFinished(val agent : Actor, val pipeline: String, val stage: String, val revision: String) extends JobMessage
