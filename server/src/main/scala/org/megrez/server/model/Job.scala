package org.megrez.server.model

import data.{Meta, Entity}
import org.neo4j.graphdb.{DynamicRelationshipType, Node}

class Job private (val node : Node) extends Entity {
  val name = read(Job.name)
  val resources = read(Job.resources)
  val tasks = read(Job.tasks)
}

object Job extends Meta[Job] {
  val name = property[String]("name")
  val resources = property[Set[String]]("resources")
  val tasks = list("tasks", Task, DynamicRelationshipType.withName("EXECUTE"))

  def apply(node : Node) = new Job(node)
}