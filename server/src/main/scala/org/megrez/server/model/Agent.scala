package org.megrez.server.model

import data.{Repository, Entity}
import org.neo4j.graphdb.{DynamicRelationshipType, Node}
import DynamicRelationshipType._

class Agent private(val node: Node) extends Entity {
  val resources = reader(Agent.resources)

  def setResources(resources : Set[String]) {
    write(Agent.resources, resources)
  }
}

object Agent extends Repository[Agent] {
  val root = withName("AGENTS")
  val entity = withName("AGENT")

  val resources = property[Set[String]]("resources")

  def apply(node : Node) = new Agent(node)
}