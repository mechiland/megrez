package org.megrez

import java.io.InputStream

trait AgentMessage

case class JobAssignment(override val pipeline: String, val materials: Map[Material, Option[Any]], val job: Job)
        extends JobAssignmentFuture(0, pipeline, materials.map(keyvalue => (keyvalue._1.source, keyvalue._1.destination) -> keyvalue._2).toMap, job.tasks) with AgentMessage

//this type will replace JobAssignment soon
case class JobAssignmentFuture(val buildId: Int, val pipeline: String, val sources: Map[(ChangeSource, String), Option[Any]], val tasks: List[Task]) extends AgentMessage

case class JobFailed(val reason: String = "") extends AgentMessage
case class JobCompleted(val result: String = "") extends AgentMessage

case class ConsoleOutput(val output: String) extends AgentMessage
case class ArtifactStream(val input: InputStream)

object Stop
