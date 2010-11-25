package org.megrez.server.model

import data.{Entity, Meta}
import org.neo4j.graphdb.{DynamicRelationshipType, Node}

class Build private (val node: Node) extends Entity {
  val pipeline = read(Build.pipeline)
}

object Build extends Meta[Build] {
  val pipeline = reference("pipeline", Pipeline, DynamicRelationshipType.withName("FOR_PIPELINE"))

  def apply(node : Node) = new Build(node)
}