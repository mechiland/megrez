package org.megrez.server.data

import org.neo4j.graphdb._
import org.neo4j.graphdb.Traverser.Order
import collection.mutable.HashSet

trait BelongsToGraph {
  protected var graph: GraphDatabaseService = null

  private[data] def assign(graph: GraphDatabaseService) {
    this.graph = graph
  }

  protected def transaction[T](operation: => T): T = {
    val transaction = graph.beginTx
    try {
      val result = operation
      transaction.success
      result
    } finally {
      transaction.finish
    }
  }

  protected def returnable(evaluator: TraversalPosition => Boolean) = new ReturnableEvaluator() {
    def isReturnableNode(position: TraversalPosition) = evaluator(position)
  }

  protected def has(node: Node, relationshipType: RelationshipType) = node.getSingleRelationship(relationshipType, Direction.OUTGOING) != null

  Graph.register(this)
}

object Graph {
  private var graph: GraphDatabaseService = null
  private val holders = HashSet[BelongsToGraph]()

  def from(graph: GraphDatabaseService) {
    this.graph = graph
    holders.foreach(_ assign graph)
  }

  private[data] def register(holder: BelongsToGraph) {
    holder.assign(graph)
    holders += holder
  }
}

trait Metadata[EntityType <: Entity] extends BelongsToGraph {
  def property[T](name: String) = Property[T](name)
  
  def list[T <: Entity](metadata : Metadata[T], start: RelationshipType, link: RelationshipType) = NodeList[T](metadata, start, link)

  def apply(node : Node) : EntityType
}

trait Repository[EntityType <: Entity] extends BelongsToGraph {
  val referenceType: RelationshipType
  
  protected def reference = Option(graph.getReferenceNode.getSingleRelationship(referenceType, Direction.OUTGOING)).getOrElse(
    graph.getReferenceNode.createRelationshipTo(graph.createNode, referenceType)).getEndNode  
}

trait Entity extends BelongsToGraph {
  val node: Node

  class PropertyAccessor[T](val property: Property[T]) {    
    def apply() = node.getProperty(property.name).asInstanceOf[T]

    def apply(value: T) = node.setProperty(property.name, value)
  }

  class ListAccessor[T <: Entity](val list: NodeList[T]) {
    import Order._
    import StopEvaluator._
    import Direction._
    import ReturnableEvaluator._
    import scala.collection.JavaConversions._
    
    def apply() : List[T] = {
      val start = Option(node.getSingleRelationship(list.start, OUTGOING))
      start match {
        case Some(relationship: Relationship) =>
          relationship.getEndNode.traverse(BREADTH_FIRST, END_OF_GRAPH, ALL, list.next, OUTGOING).getAllNodes.map(list.metadata(_)).toList
        case None => List[T]()
      }
    }

    def <<(element: T) {
      val start = Option(node.getSingleRelationship(list.start, Direction.OUTGOING))
      start match {
        case Some(relationship: Relationship) =>
          val nodes = relationship.getEndNode.traverse(BREADTH_FIRST, END_OF_GRAPH,
            returnable(position => !has(position.currentNode, list.next)), list.next, OUTGOING).getAllNodes
          val last = nodes.iterator.next
          last.createRelationshipTo(element.node, list.next)
        case None =>
          node.createRelationshipTo(element.node, list.start)
      }
    }
  }

  protected def property[T](property: Property[T]) = new PropertyAccessor[T](property)
  protected def list[T <: Entity](list: NodeList[T]) = new ListAccessor[T](list)

  def updateAttributes(attributes : Map[String, Any])
}


case class Property[T](name: String)
case class NodeList[T <: Entity](metadata : Metadata[T], start: RelationshipType, next: RelationshipType)