package org.megrez.server.model.data

import collection.mutable.{HashMap, HashSet}
import org.neo4j.graphdb.{DynamicRelationshipType, GraphDatabaseService, RelationshipType, Node}

trait Meta[EntityType <: Entity] {
  import Graph._
  private val _properties = HashMap[Property[_], PropertyConverter[_]]()
  private val _references = HashSet[Reference[_ <: Entity]]()
  private val _referenceLists = HashSet[ReferenceList[_ <: Entity]]()
  private val _referenceSets = HashSet[ReferenceSet[_ <: Entity]]()

  private[data] var graph: GraphDatabaseService = null

  def apply(node: Node): EntityType

  def apply(attributes: Map[String, Any]): EntityType = apply(updateAttribute(createNode, attributes))

  protected def createNode = graph.update(_.createNode)

  private def updateAttribute(node: Node, attributes: Map[String, Any]): Node = {
    node.update {
      node =>
        for ((property, converter) <- _properties)
          attributes.get(property.name).map(value => node.setProperty(property.name, converter.from(value)))
        
        for (reference <- _references)
          attributes.get(reference.name) match {
            case Some(data: Map[String, Any]) =>
              node.createRelationshipTo(reference.meta(data).node, reference.relationship)
            case _ => throw new Exception()
          }

        for (list <- _referenceLists)
          attributes.get(list.name) match {
            case Some(data: List[Map[String, Any]]) =>
              val start = list.meta(data.head)
              data.tail.foldLeft(start.node)((pre, next) => pre.createRelationshipTo(list.meta(next).node,
                DynamicRelationshipType.withName("NEXT")).getEndNode)
              node.createRelationshipTo(start.node, list.relationship)
            case _ => throw new Exception()
          }

        for (set <- _referenceSets)
          attributes.get(set.name) match {
            case Some(data: List[Map[String, Any]]) =>
              data.foreach(data => node.createRelationshipTo(set.meta(data).node, set.relationship))
            case _ => throw new Exception()
          }      
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

  def list[T <: Entity](name: String, meta: Meta[T], relationship: RelationshipType) = {
    val list = ReferenceList[T](name, meta, relationship)
    _referenceLists += list
    list
  }

  def set[T <: Entity](name: String, meta: Meta[T], relationship: RelationshipType) = {
    val set = ReferenceSet[T](name, meta, relationship)
    _referenceSets += set
    set
  }


  trait PropertyConverter[T] {
    def from(value: Any): T

    def to(value: T): Any
  }

  class PrimitiveConverter[T] extends PropertyConverter[T] {
    def from(value: Any): T = value.asInstanceOf[T]

    def to(value: T): Any = value
  }

  class ArrayConverter[T](func: List[T] => Array[T]) extends PropertyConverter[Array[T]] {
    def from(value: Any): Array[T] = value match {
      case list: List[T] => func(list)
      case _ => throw new Exception()
    }

    def to(value: Array[T]) = value.toList
  }

  protected implicit def stringConverter = new PrimitiveConverter[String]()

  protected implicit def intConverter = new PrimitiveConverter[Int]()

  protected implicit def stringArrayConverter = new ArrayConverter[String](_.toArray)

  protected implicit def intArrayConverter = new ArrayConverter[Int](_.toArray)
}

case class Property[T](name: String)
case class Reference[T <: Entity](name: String, meta: Meta[T], relationship: RelationshipType)
case class ReferenceList[T <: Entity](name: String, meta: Meta[T], relationship: RelationshipType)
case class ReferenceSet[T <: Entity](name: String, meta: Meta[T], relationship: RelationshipType)
//case class ReferenceMap[T <: Entity]
