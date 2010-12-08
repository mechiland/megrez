package org.megrez.server.model.data

import collection.mutable.HashSet
import org.neo4j.graphdb.{DynamicRelationshipType, GraphDatabaseService, RelationshipType, Node}

trait Meta[EntityType <: Entity] {
  import Graph._
  private val _properties = HashSet[Property[_]]()
  private val _enumerations = HashSet[Enum[_]]()
  private val _references = HashSet[Reference[_ <: Entity]]()
  private val _referenceLists = HashSet[ReferenceList[_ <: Entity]]()
  private val _referenceSets = HashSet[ReferenceSet[_ <: Entity]]()

  private[data] var graph: GraphDatabaseService = null

  def apply(node: Node): EntityType

  def apply(attributes: Map[String, Any]): EntityType = apply(updateAttribute(createNode, attributes))

//  def apply(attributes: Pair[String, Any]*) : EntityType = apply(attributes.toMap)

  protected def createNode = graph.update(_.createNode)

  private def updateAttribute(node: Node, attributes: Map[String, Any]): Node = {
    node.update {
      node =>        
        updateProperties(attributes, node)
        updateEnumerations(attributes, node)
        updateReferences(attributes, node)
        updateReferenceLists(attributes, node)
        updateReferenceSets(attributes, node)
    }
    node
  }

  private def updateProperties(attributes: Map[String, Any], node: Node) {
    for (property <- _properties)
      attributes.get(property.name).map(value => node.setProperty(property.name, property.converter.to(value)))
  }

  private def updateEnumerations(attributes: Map[String, Any], node: Node) {
    for (enum <- _enumerations)
      attributes.get(enum.name).map(value => node.setProperty(enum.name, value.toString))
  }

  private def updateReferences(attributes: Map[String, Any], node: Node) {
    for (reference <- _references)
      attributes.get(reference.name) match {
        case Some(data: Map[String, Any]) =>
          node.createRelationshipTo(reference.meta(data).node, reference.relationship)
        case Some(entity: Entity) =>
          if (reference.manifest.erasure.isAssignableFrom(entity.getClass))
            node.createRelationshipTo(entity.node, reference.relationship)
          else throw new Exception()
        case _ =>
      }
  }

  private def updateReferenceLists(attributes: Map[String, Any], node: Node) {
    for (list <- _referenceLists)
      attributes.get(list.name) match {
        case Some(data: List[_]) =>
          val entities = data.map(_ match {
            case data: Map[String, Any] => list.meta(data)
            case entity: Entity => entity
          })
          if (!entities.isEmpty) {
            entities.tail.foldLeft(entities.head.node)((pre, next) => pre.createRelationshipTo(next.node,
              DynamicRelationshipType.withName("NEXT")).getEndNode)
            node.createRelationshipTo(entities.head.node, list.relationship)
          }
        case _ =>
      }
  }

  private def updateReferenceSets(attributes: Map[String, Any], node: Node) {
    for (set <- _referenceSets)
      attributes.get(set.name) match {
        case Some(data: List[_]) =>
          data.foreach(data => node.createRelationshipTo((data match {
            case data: Map[String, Any] => set.meta(data)
            case entity: Entity => entity
          }).node, set.relationship))
        case _ => throw new Exception()
      }
  }

  def property[T](name: String)(implicit converter: PropertyConverter[T], manifest: Manifest[T]) = {
    val property = Property[T](name, converter, manifest)
    _properties += property
    property
  }

  def enum[T <: Enumeration](name: String, enumeration: T) = {    
    val enum = Enum[T](name, enumeration)
    _enumerations += enum
    enum
  }

  def reference[T <: Entity](name: String, meta: Meta[T], relationship: RelationshipType)(implicit manifest: Manifest[T]) = {
    val reference = Reference[T](name, meta, relationship, manifest)
    _references += reference
    reference
  }

  def list[T <: Entity](name: String, meta: Meta[T], relationship: RelationshipType)(implicit manifest: Manifest[T]) = {
    val list = ReferenceList[T](name, meta, relationship, manifest)
    _referenceLists += list
    list
  }

  def set[T <: Entity](name: String, meta: Meta[T], relationship: RelationshipType)(implicit manifest: Manifest[T]) = {
    val set = ReferenceSet[T](name, meta, relationship, manifest)
    _referenceSets += set
    set
  }

  class PrimitiveConverter[T] extends PropertyConverter[T] {
    def from(value: Any): T = value.asInstanceOf[T]

    def to(value: Any): Any = value
  }

  class ArrayConverter[T](implicit func: List[T] => Array[T]) extends PropertyConverter[Array[T]] {
    def from(value: Any): Array[T] = value match {
      case list: Array[T] => list
      case _ => throw new Exception()
    }

    def to(value: Any) = value match {
      case set: List[T] => func(set)
      case array: Array[T] => array
      case _ => throw new Exception()
    }
  }

  class SetPropertyConverter[T](implicit func: List[T] => Array[T]) extends PropertyConverter[Set[T]] {
    def from(value: Any): Set[T] = value match {
      case list: Array[T] => list.toSet
      case null => Set[T]()
      case _ => throw new Exception()
    }

    def to(value: Any) = value match {
      case set: List[T] => func(set)
      case _ => println(value); throw new Exception()
    }
  }

  protected implicit def stringConverter = new PrimitiveConverter[String]()

  protected implicit def intConverter = new PrimitiveConverter[Int]()

  protected implicit def longConverter = new PrimitiveConverter[Long]()

  protected implicit def stringArrayConverter = new ArrayConverter[String]()

  protected implicit def intArrayConverter = new ArrayConverter[Int]()

  protected implicit def stringSetConverter = new SetPropertyConverter[String]()

  private implicit def stringListToArray: List[String] => Array[String] = _.toArray

  private implicit def intListToArray: List[Int] => Array[Int] = _.toArray
}

trait PropertyConverter[T] {
  def from(value: Any): T

  def to(value: Any): Any
}

case class Property[T](name: String, converter: PropertyConverter[T], manifest: Manifest[T])
case class Enum[T <: Enumeration](name: String, enumeration: T)
case class Reference[T <: Entity](name: String, meta: Meta[T], relationship: RelationshipType, manifest: Manifest[T])
case class ReferenceList[T <: Entity](name: String, meta: Meta[T], relationship: RelationshipType, manifest: Manifest[T])
case class ReferenceSet[T <: Entity](name: String, meta: Meta[T], relationship: RelationshipType, manifest: Manifest[T])

