package org.megrez.server.model.data

import org.neo4j.graphdb.Traverser.Order
import org.neo4j.graphdb._

trait Entity {
  import Graph._

  import scala.collection.JavaConversions._

  val node: Node

  protected def accessor[T](property: Property[T]) = new PropertyAccessor[T](node, property)

  protected def reader[T <: Entity](list: ReferenceList[T]) = new ReferenceListReader[T] {val reference = list}

  protected def reader[T <: Entity](set: ReferenceSet[T]) = new ReferenceSetReader[T] {val reference = set}

  protected def reader[T <: Entity](target: Reference[T]) = new ReferenceReader[T] {val reference = target}

  protected def read[T](property: Property[T]) = node.getProperty(property.name).asInstanceOf[T]

  protected def read[T <: Entity](reference: Reference[T]) = Option(node.getSingleRelationship(reference.relationship, Direction.OUTGOING)).map(rel => reference.meta(rel.getEndNode)).getOrElse(null).asInstanceOf[T]  

  protected def read[T <: Entity](list: ReferenceList[T]) = Option(node.getSingleRelationship(list.relationship, Direction.OUTGOING)).map {
    rel =>
      rel.getEndNode.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, ReturnableEvaluator.ALL,
        DynamicRelationshipType.withName("NEXT"), Direction.OUTGOING).getAllNodes.map(list.meta(_)).toList
  }.getOrElse(List[T]())

  protected def read[T <: Entity](set : ReferenceSet[T]) = node.getRelationships(set.relationship, Direction.OUTGOING).map(rel => set.meta(rel.getEndNode)).toSet

  trait PropertyReader[T] {
    val property: Property[T]

    def apply() = if (node.hasProperty(property.name)) Some(read(property)) else None
  }

  trait PropertyWriter[T] {
    val node: Node
    val property: Property[T]

    def apply(value: T): Unit = node.update(_.setProperty(property.name, value))
  }

  class PropertyAccessor[T](val node: Node, val property: Property[T]) extends PropertyReader[T] with PropertyWriter[T]

  trait ReferenceReader[T <: Entity] {    
    val reference: Reference[T]

    def apply() = Option(node.getSingleRelationship(reference.relationship, Direction.OUTGOING)).map(rel => reference.meta(rel.getEndNode))
  }

  trait ReferenceListReader[T <: Entity] {
    val reference: ReferenceList[T]

    def apply() = read(reference)
  }

  trait ReferenceSetReader[T <: Entity] {
    val reference: ReferenceSet[T]

    def apply() = read(reference)
  }
}
