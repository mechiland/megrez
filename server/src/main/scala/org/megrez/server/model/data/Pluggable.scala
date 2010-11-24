package org.megrez.server.model.data

import collection.mutable.HashMap
import org.neo4j.graphdb.Node

trait Pluggable[EntityType <: Entity] extends Meta[EntityType] {
  private val _plugins = HashMap[String, Meta[_ <: EntityType]]()

  private[data] def register(name: String, meta: Meta[_ <: EntityType]) {
    _plugins.put(name, meta)
  }

  def apply(node: Node): EntityType =
    _plugins.get(node.getProperty("type").toString) match {
      case Some(meta) => meta(node)
      case _ => throw new Exception()
    }

  override def apply(data: Map[String, Any]) = {
    data.get("type") match {
      case Some(nodeType: String) =>
        _plugins.get(nodeType) match {
          case Some(meta) => meta(data)
          case _ => throw new Exception()
        }
      case _ => throw new Exception()
    }
  }
}

abstract class Plugin[EntityType <: Entity](pluggable: Pluggable[_ >: EntityType], name: String) extends Meta[EntityType] {
  import Graph._
  pluggable.register(name, this)

  override protected def createNode = graph.update {
    graph =>
      val node = graph.createNode
      node.setProperty("type", name)
      node
  }
}