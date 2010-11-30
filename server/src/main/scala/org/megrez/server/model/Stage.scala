package org.megrez.server.model

import data.{Meta, Entity}
import org.neo4j.graphdb.{Direction, Node, DynamicRelationshipType}

class Stage private (val node : Node) extends Entity {
  val name = read(Stage.name)
  val jobs = read(Stage.jobs)

  def next = Option(node.getSingleRelationship(DynamicRelationshipType.withName("NEXT"), Direction.OUTGOING)).map(rel => Stage(rel.getEndNode))
}

object Stage extends Meta[Stage] {
  val name = property[String]("name")
  val jobs = set("jobs", Job, DynamicRelationshipType.withName("RUN"))

  def apply(node : Node) = new Stage(node)
}