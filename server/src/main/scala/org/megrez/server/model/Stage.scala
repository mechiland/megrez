package org.megrez.server.model

import data.{Meta, Entity}
import org.neo4j.graphdb.{Node, DynamicRelationshipType}

class Stage private (val node : Node) extends Entity with org.megrez.model.Stage {
  val name = read(Stage.name)
  val jobs = read(Stage.jobs)
}

object Stage extends Meta[Stage] {
  val name = property[String]("name")
  val jobs = set("jobs", Job, DynamicRelationshipType.withName("RUN"))

  def apply(node : Node) = new Stage(node)
}