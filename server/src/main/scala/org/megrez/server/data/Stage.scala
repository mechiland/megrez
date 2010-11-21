package org.megrez.server.data

import org.neo4j.graphdb.Node


class Stage(val node: Node) extends Entity {
  val name = property(Stage.name)

  def updateAttributes(data: Map[String, Any]) {
    transaction {
      name(data("name").asInstanceOf[String])
    }
  }
}

object Stage extends Metadata[Stage] {
  val name = property[String]("name")

  def apply(node: Node) = new Stage(node)
}