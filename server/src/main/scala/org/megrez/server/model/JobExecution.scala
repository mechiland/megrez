package org.megrez.server.model

import data.{Entity, Meta}
import org.neo4j.graphdb.Node


class JobExecution private(val node : Node) extends Entity {
  
}

object JobExecution extends Meta[JobExecution] {

  def apply(node : Node) = new JobExecution(node)
}