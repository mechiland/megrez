package org.megrez.server.model

import data.{Meta, Entity}
import org.neo4j.graphdb.{DynamicRelationshipType, Node}
import DynamicRelationshipType._

class StageExecution private (val node: Node) extends Entity {
  val stage = read(StageExecution.stage)
  val jobs = read(StageExecution.jobs)
}

object StageExecution extends Meta[StageExecution] {
  val stage = reference("stage", Stage, withName("FOR_STAGE"))
  val jobs = list("jobs", JobExecution, withName("RUN"))

  def apply(node : Node) = new StageExecution(node)

  def apply(stage : Stage) : StageExecution = StageExecution(Map("stage" -> stage, "jobs" -> stage.jobs.map(JobExecution(_)).toList))
}