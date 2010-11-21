package org.megrez.server.data

import org.neo4j.graphdb.{DynamicRelationshipType, Node}

class Stage private (val node: Node) extends Entity {
  val name = property(Stage.name)
  val jobs = set(Stage.jobs)
}

object Stage extends Metadata[Stage] {
  import DynamicRelationshipType._

  val name = property[String]("name")
  val jobs = set("jobs", Job, withName("RUN_JOB"))

  def apply(node: Node) = new Stage(node)
}
