package org.megrez.server.model

import data.{Entity, Meta}
import org.neo4j.graphdb.{DynamicRelationshipType, Node}
import DynamicRelationshipType._

class Build private(val node: Node) extends Entity {
  val pipeline = read(Build.pipeline)
  val current = reader(Build.current)
  
  write(Build.current, pipeline.stages.head)  
}

object Build extends Meta[Build] {
  val pipeline = reference("pipeline", Pipeline, withName("FOR_PIPELINE"))
  val current = reference("current", Stage, withName("CURRENT_STAGE"))
  val stages = list("stages", StageExecution, withName("STARTED"))
  

  def apply(node: Node) = new Build(node)
}