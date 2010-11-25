package org.megrez.server.model

import data.{Meta, Entity}
import org.neo4j.graphdb.{Node, DynamicRelationshipType}

class Material private(val node: Node) extends Entity with org.megrez.model.Material {
  val destination = read(Material.destination)
  val changeSource = read(Material.changeSource)
  
}

object Material extends Meta[Material] {
  val destination = property[String]("destination")
  val changeSource = reference("source", ChangeSource, DynamicRelationshipType.withName("SOURCE"))

  def apply(node : Node) = new Material(node)
}