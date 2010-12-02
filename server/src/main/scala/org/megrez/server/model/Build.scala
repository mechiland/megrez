package org.megrez.server.model

import data.{Repository, Entity, Meta}
import org.neo4j.graphdb.{DynamicRelationshipType, Node}
import DynamicRelationshipType._

class Build private(val node: Node) extends Entity {
  import Build.Status._

  val pipeline = read(Build.pipeline)
  val changes = read(Build.changes)
  val status = reader(Build.status)
  val stages = reader(Build.stages)

  def next = read { node => current.map(checkCurrentStage(_)).getOrElse(advance)}

  private def checkCurrentStage(current: StageExecution) =
    current.status match {
      case StageExecution.Status.Completed => advance
      case StageExecution.Status.Failed => write(Build.status, Failed); Nil
      case StageExecution.Status.Failing => write(Build.status, Failing); Nil
      case _ => Nil
    }

  private def nextStage = current.map(_.stage.next.map(stage => StageExecution(stage))).getOrElse(Some(StageExecution(pipeline.stages.head)))

  private def current = {
    val executions = stages()
    if (executions.isEmpty) None else Some(executions.last)
  }

  private def advance = {
    nextStage match {
      case Some(execution) =>
        append(Build.stages, execution)
        execution.jobs
      case None =>
        write(Build.status, Completed)
        Nil
    }
  }
}

object Build extends Repository[Build] {
  val root = withName("BUILDS")
  val entity = withName("BUILD")  

  val pipeline = reference("pipeline", Pipeline, withName("FOR_PIPELINE"))
  val changes = set("changes", Change, withName("OF_CHANGES"))
  
  val status = enum("status", Status)
  val stages = list("stages", StageExecution, withName("STARTED"))

  def apply(node: Node) = new Build(node)

  def apply(pipeline: Pipeline, changes : Set[Change]): Build = Build(Map("pipeline" -> pipeline, "status" -> Status.Building, "changes" -> changes.toList))

  object Status extends Enumeration {
    type Status = Value
    val Building, Failing, Completed, Failed = Value
  }
}


 