package org.megrez.server.data

import org.neo4j.graphdb.Node


class Stage private (val node: Node) extends Entity {
  val name = property(Stage.name)
}

object Stage extends Metadata[Stage] {
  val name = property[String]("name")

  def apply(node: Node) = new Stage(node)
}