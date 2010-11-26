package org.megrez.server.model.data

import org.neo4j.graphdb.Traverser.Order
import org.neo4j.graphdb._

trait Entity {
  import Graph._

  import scala.collection.JavaConversions._

  val node: Node

  val id = node.getId

  protected def read[T](property: Property[T]) = property.converter.from(if (node.hasProperty(property.name)) node.getProperty(property.name) else null)

  protected def write[T](property: Property[T], value: T) = node.update(_.setProperty(property.name, property.converter.to(value)))

  protected def read[T <: Entity](reference: Reference[T]) = Option(node.getSingleRelationship(reference.relationship, Direction.OUTGOING)).map(rel => reference.meta(rel.getEndNode)).getOrElse(null).asInstanceOf[T]

  protected def write[T <: Entity](reference: Reference[T], value: T) {
    val relationship = node.getSingleRelationship(reference.relationship, Direction.OUTGOING)
    node.update {
      node =>
        Option(relationship).map(_.delete)        
        node.createRelationshipTo(value.node, reference.relationship)
    }
  }

  protected def read[T <: Entity](list: ReferenceList[T]) = Option(node.getSingleRelationship(list.relationship, Direction.OUTGOING)).map {
    rel =>
      rel.getEndNode.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, ReturnableEvaluator.ALL,
        DynamicRelationshipType.withName("NEXT"), Direction.OUTGOING).getAllNodes.map(list.meta(_)).toList
  }.getOrElse(List[T]())

  protected def read[T <: Entity](set: ReferenceSet[T]) = node.getRelationships(set.relationship, Direction.OUTGOING).map(rel => set.meta(rel.getEndNode)).toSet

  override def hashCode = node.hashCode

  override def equals(other: Any) = if (other.isInstanceOf[Entity]) other.asInstanceOf[Entity].id == id else false

  trait ReferenceReader[T <: Entity] {
    val reference: Reference[T]

    def apply() = read(reference)
  }

  class ReferenceAccessor[T <: Entity](val reference: Reference[T]) extends ReferenceReader[T] {
    def apply(value: T) = write(reference, value)
  }

  protected def reader[T <: Entity](ref: Reference[T]) = new ReferenceReader[T] {val reference = ref}

  protected def accessor[T <: Entity](ref: Reference[T]) = new ReferenceAccessor[T](ref)
}

