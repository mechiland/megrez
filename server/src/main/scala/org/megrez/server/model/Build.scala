package org.megrez.server.model

import data.{Entity, Meta}
import org.neo4j.graphdb.{DynamicRelationshipType, Node}
import DynamicRelationshipType._

class Build private(val node: Node) extends Entity {
  val pipeline = read(Build.pipeline)
  val stages = reader(Build.stages)

  def current = stages().last.stage
}

object Build extends Meta[Build] {
  val pipeline = reference("pipeline", Pipeline, withName("FOR_PIPELINE"))
  val status = property[String]("status")
  val stages = list("stages", StageExecution, withName("STARTED"))


  def apply(node: Node) = new Build(node)

  def start(pipeline: Pipeline) = {
    val build = Build(Map("pipeline" -> pipeline))
    build.append(stages, StageExecution(pipeline.stages.head))
    build
  }
}