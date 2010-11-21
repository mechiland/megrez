package org.megrez

import java.io.InputStream

trait AgentMessage

case class JobAssignment(val pipeline: String, val materials: Map[Material, Option[Any]], val job: Job) extends AgentMessage

case class JobFailed(val reason: String = "") extends AgentMessage
case class JobCompleted(val result: String = "") extends AgentMessage

case class ConsoleOutput(val output: String) extends AgentMessage
case class ArtifactStream(val input: InputStream)

object Stop