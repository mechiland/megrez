package org.megrez.server.model.data

import org.neo4j.graphdb.{Direction, RelationshipType}

trait Repository[T <: Entity] extends Meta[T] {
  import Graph._
  
  val root: RelationshipType
  val entity: RelationshipType

  private def reference = Option(graph.getReferenceNode.getSingleRelationship(root, Direction.OUTGOING)).getOrElse(
    graph.getReferenceNode.createRelationshipTo(graph.createNode, root)).getEndNode

  override protected def createNode = graph.update(graph => reference.createRelationshipTo(graph.createNode, entity).getEndNode)
}