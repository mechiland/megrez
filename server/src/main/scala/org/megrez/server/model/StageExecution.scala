package org.megrez.server.model

import data.{Meta, Entity}
import org.neo4j.graphdb.{DynamicRelationshipType, Node}
import DynamicRelationshipType._

class StageExecution private(val node: Node) extends Entity {
  import StageExecution.Status._
  val stage = read(StageExecution.stage)
  val jobs = read(StageExecution.jobs)

  def status = {
    val executions = jobs.groupBy(_.status().asInstanceOf[Enumeration#Value])
    def count(status: Enumeration#Value) = executions.get(status).map(_.size).getOrElse(0)
    val numberOfScheduled = count(JobExecution.Status.Scheduled)
    val numberOfRunning = count(JobExecution.Status.Running)
    val numberOfFailed = count(JobExecution.Status.Failed)
    val numberOfCompleted = count(JobExecution.Status.Completed)

    if (numberOfScheduled == jobs.size) Scheduled
    else if (numberOfCompleted == jobs.size) Completed
    else if (numberOfFailed + numberOfCompleted == jobs.size) Failed
    else if (numberOfFailed != 0 && (numberOfRunning != 0 || numberOfScheduled != 0)) Failing
    else Running
  }
}

object StageExecution extends Meta[StageExecution] {
  val stage = reference("stage", Stage, withName("FOR_STAGE"))
  val jobs = list("jobs", JobExecution, withName("RUN"))

  def apply(node: Node) = new StageExecution(node)

  def apply(stage: Stage): StageExecution = StageExecution(Map("stage" -> stage, "jobs" -> stage.jobs.map(JobExecution(_)).toList))

  object Status extends Enumeration {
    type Status = Value
    val Scheduled, Running, Failing, Completed, Failed = Value
  }
}