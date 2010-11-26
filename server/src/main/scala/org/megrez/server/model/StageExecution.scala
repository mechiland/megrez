package org.megrez.server.model

import data.{Meta, Entity}
import org.neo4j.graphdb.{DynamicRelationshipType, Node}
import DynamicRelationshipType._

class StageExecution private (val node: Node) extends Entity {
  val stage = read(StageExecution.stage)
}

object StageExecution extends Meta[StageExecution] {
  val stage = reference("stage", Pipeline, withName("FOR_STAGE"))

  def apply(node : Node) = new StageExecution(node)
}