package org.megrez

trait AgentMessage

case class JobAssignment(val pipeline: String, val materials: Map[Material, Option[Any]], val job: Job) extends AgentMessage

case class JobFailed(val reason: String) extends AgentMessage
case class JobCompleted(val result: String = "") extends AgentMessage

object Stop