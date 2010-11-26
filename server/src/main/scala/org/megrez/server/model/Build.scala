package org.megrez.server.model

import data.{Entity, Meta}
import org.neo4j.graphdb.{DynamicRelationshipType, Node}
import DynamicRelationshipType._

class Build private(val node: Node) extends Entity {
  import Build.Status._

  val pipeline = read(Build.pipeline)
  val status = reader(Build.status)
  val stages = reader(Build.stages)

  def next = {    
    if (status() == Advance) {
      nextStage match {
        case Some(execution) =>
          append(Build.stages, execution)
          write(Build.status, Running)
          execution.jobs
        case None =>          
          write(Build.status, Completed)
          Nil
      }
    } else Nil
  }

  private def nextStage: Option[StageExecution] = {
    val executions = stages()
    if (executions.isEmpty)
      Some(StageExecution(pipeline.stages.head))
    else
      executions.last.stage.next.map(StageExecution(_))
  }
}

object Build extends Meta[Build] {
  val pipeline = reference("pipeline", Pipeline, withName("FOR_PIPELINE"))
  val status = enum("status", Status)
  val stages = list("stages", StageExecution, withName("STARTED"))

  def apply(node: Node) = new Build(node)

  def apply(pipeline: Pipeline): Build = Build(Map("pipeline" -> pipeline, "status" -> Status.Advance))

  object Status extends Enumeration {
    type Status = Value
    val Advance, Running, Failing, Completed, Failed = Value
  }
}


 