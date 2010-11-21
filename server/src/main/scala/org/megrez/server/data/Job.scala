package org.megrez.server.data

import org.neo4j.graphdb.Node

class Job private (val node : Node) extends Entity {
  val name = property(Job.name)
}

object Job extends Metadata[Job] {
  val name = property[String]("name")

  def apply(node: Node) = new Job(node)
}