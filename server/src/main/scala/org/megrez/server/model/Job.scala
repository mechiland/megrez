package org.megrez.server.model

import data.{Meta, Entity}
import org.neo4j.graphdb.{DynamicRelationshipType, Node}

class Job private (val node : Node) extends Entity with org.megrez.model.Job {
  val name = read(Job.name)
  val tasks = read(Job.tasks)
}

object Job extends Meta[Job] {
  val name = property[String]("name")
  val tasks = list("tasks", Task, DynamicRelationshipType.withName("EXECUTE"))

  def apply(node : Node) = new Job(node)
}