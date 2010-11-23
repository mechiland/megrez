package org.megrez.server.model.data

import org.neo4j.graphdb.{GraphDatabaseService, RelationshipType, Node}
import collection.mutable.{HashMap, HashSet}

trait Meta[EntityType <: Entity] {
  import Graph._
  private val _properties = HashMap[Property[_], PropertyConverter[_]]()
  private val _references = HashSet[Reference[_ <: Entity]]()

  private[data] var graph: GraphDatabaseService = null

  def apply(node: Node): EntityType

  def apply(attributes: Map[String, Any]): EntityType = apply(updateAttribute(createNode, attributes))

  protected def createNode = graph.update(_.createNode)

  private def updateAttribute(node: Node, attributes: Map[String, Any]): Node = {
    node.update {
      node =>
        for ((property, converter) <- _properties)
          attributes.get(property.name).map(value => node.setProperty(property.name, converter.from(value)))
    }
    node
  }

  def property[T](name: String)(implicit converter: PropertyConverter[T]) = {
    val property = Property[T](name)
    _properties.put(property, converter)
    property
  }

  def reference[T <: Entity](name: String, meta: Meta[T], relationship: RelationshipType) = {
    val reference = Reference[T](name, meta, relationship)
    _references += reference
    reference
  }

  trait PropertyConverter[T] {
    def from(value: Any): T

    def to(value: T): Any
  }

  class PrimitiveConverter[T] extends PropertyConverter[T] {
    def from(value: Any): T = value.asInstanceOf[T]

    def to(value: T): Any = value
  }

  class ArrayConverter[T](func : List[T] => Array[T]) extends PropertyConverter[Array[T]] {
    def from(value : Any) : Array[T] = value match {
      case list : List[T] => func(list)
      case _ => throw new Exception()
    }
    def to(value : Array[T]) = value.toList
  }

  protected implicit def stringConverter = new PrimitiveConverter[String]()
  protected implicit def intConverter = new PrimitiveConverter[Int]()
  
  protected implicit def stringArrayConverter = new ArrayConverter[String](_.toArray)
  protected implicit def intArrayConverter = new ArrayConverter[Int](_.toArray)
}

case class Property[T](name: String)
case class Reference[T <: Entity](name: String, metadata: Meta[T], relationship: RelationshipType)
