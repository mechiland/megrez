package org.megrez

import java.io.InputStream

trait AgentMessage

//this type will replace JobAssignment soon
case class JobAssignmentFuture(val buildId: Int, val pipeline: String, val sources: Map[(ChangeSource, String), Option[Any]], val tasks: List[Task]) extends AgentMessage

case class JobFailed(val reason: String = "") extends AgentMessage
case class JobCompleted(val result: String = "") extends AgentMessage

case class ConsoleOutput(val output: String) extends AgentMessage
case class ArtifactStream(val input: InputStream)

object Stop
