package org.megrez.server.data

import org.neo4j.graphdb._
import org.neo4j.graphdb.Traverser.Order
import collection.mutable.{HashMap, HashSet}

trait BelongsToGraph {
  import Order._
  import StopEvaluator._
  import Direction._

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

  protected def appendToList(node: Node, element: Node, start: RelationshipType, next: RelationshipType) {
    Option(node.getSingleRelationship(start, Direction.OUTGOING)) match {
      case Some(relationship: Relationship) =>
        val nodes = relationship.getEndNode.traverse(BREADTH_FIRST, END_OF_GRAPH,
          returnable(position => !has(position.currentNode, next)), next, OUTGOING).getAllNodes
        val last = nodes.iterator.next
        last.createRelationshipTo(element, next)
      case None =>
        node.createRelationshipTo(element, start)
    }
  }

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
  private val properties = HashMap[Property[_], PropertyReader[_]]()
  private val lists = HashSet[NodeList[_ <: Entity]]()
  private val sets = HashSet[NodeSet[_ <: Entity]]()

  implicit object StringPropertyReader extends PropertyReader[String] {
    def read(data: Any) = data.toString
  }

  def property[T](name: String)(implicit reader: PropertyReader[T]) = {
    val property = Property[T](name)
    properties.put(property, reader)
    property
  }

  def list[T <: Entity](name: String, metadata: Metadata[T], start: RelationshipType, link: RelationshipType) = {
    val list = NodeList[T](name, metadata, start, link)
    lists.add(list)
    list
  }

  def set[T <: Entity](name: String, metadata: Metadata[T], link: RelationshipType) = {
    val list = NodeSet[T](name, metadata, link)
    sets.add(list)
    list
  }


  def apply(node: Node): EntityType

  def apply(node: Node, data: Map[String, Any]): EntityType = {
    val entity = apply(node)
    updateAttributes(entity, data)
    entity
  }

  def updateAttributes(entity: EntityType, data: Map[String, Any]) {
    transaction {
      for (Pair(property, reader) <- properties)
        entity.node.setProperty(property.name, reader.read(data(property.name)))
      for (list <- lists)
        data.get(list.name) match {
          case Some(listData: List[Map[String, Any]]) =>
            listData.foreach {
              data =>
                val listEntity = list.metadata(graph.createNode, data)
                appendToList(entity.node, listEntity.node, list.start, list.next)
            }
          case _ =>
        }
      for (set <- sets)
        data.get(set.name) match {
          case Some(setData : List[Map[String, Any]]) =>
            setData.foreach { data =>
              val setEntity = set.metadata(graph.createNode, data)
              entity.node.createRelationshipTo(setEntity.node, set.link)
            }
          case _ =>
        }
    }
  }
}

trait PropertyReader[T] {
  def read(data: Any): T
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

    def apply(): List[T] = {
      val start = Option(node.getSingleRelationship(list.start, OUTGOING))
      start match {
        case Some(relationship: Relationship) =>
          relationship.getEndNode.traverse(BREADTH_FIRST, END_OF_GRAPH, ALL, list.next, OUTGOING).getAllNodes.map(list.metadata(_)).toList
        case None => List[T]()
      }
    }

    def <<(element: T) = appendToList(node, element.node, list.start, list.next)
  }

  class SetAccessor[T <: Entity](val set: NodeSet[T]) {
    import Direction._
    import scala.collection.JavaConversions._

    def apply(): Set[T] = node.getRelationships(set.link, OUTGOING).map(rel => set.metadata(rel.getEndNode)).toSet

    def <<(element: T) {
      node.createRelationshipTo(element.node, set.link)
    }
  }

  protected def property[T](property: Property[T]) = new PropertyAccessor[T](property)

  protected def list[T <: Entity](list: NodeList[T]) = new ListAccessor[T](list)

  protected def set[T <: Entity](set: NodeSet[T]) = new SetAccessor[T](set)
}


case class Property[T](name: String)
case class NodeSet[T <: Entity](name: String, metadata: Metadata[T], link: RelationshipType)
case class NodeList[T <: Entity](name: String, metadata: Metadata[T], start: RelationshipType, next: RelationshipType)