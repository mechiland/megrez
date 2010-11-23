package org.megrez.server.model.data

import org.neo4j.graphdb.Node

trait Entity {
  import Graph._

  val node: Node

  def accessor[T](property: Property[T]) = new PropertyAccessor[T](node, property)

  trait PropertyReader[T] {
    val node: Node
    val property: Property[T]

    def apply() = Option(node.getProperty(property.name)).map(_.asInstanceOf[T])
  }

  trait PropertyWriter[T] {
    val node: Node
    val property: Property[T]

    def apply(value: T): Unit = node.update(_.setProperty(property.name, value))
  }

  class PropertyAccessor[T](val node: Node, val property: Property[T]) extends PropertyReader[T] with PropertyWriter[T]
}
