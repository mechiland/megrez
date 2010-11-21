package org.megrez.server.data

import org.neo4j.graphdb._

class Pipeline(val node : Node) extends Entity {
  val name = property[String]("name")
}

class Pipelines(val root : Root) extends Repository[Pipeline] {
  private val reference = getReference(Relationships.pipelines)

  def createPipeline(name : String) = {
    val pipeline = new Pipeline(reference.createRelationshipTo(root.createNode, Relationships.pipeline).getEndNode)
    pipeline.name(name)
    pipeline
  }

  def create(data : Map[String, Any]) = {
    val pipeline = new Pipeline(reference.createRelationshipTo(root.createNode, Relationships.pipeline).getEndNode)
    pipeline.name(data.get("name").get.asInstanceOf[String])
    pipeline
  }
}

object Relationships {
  val pipelines = DynamicRelationshipType.withName("PIPELINES")
  val pipeline = DynamicRelationshipType.withName("PIPELINE")
}

trait Entity {
  val node : Node

  protected def property[T](name : String) = new Property[T](node, name)
}

class Property[T](node : Node ,name : String) {
  def apply() = node.getProperty(name).asInstanceOf[T]
  def apply(value : T) = node.setProperty(name, value)
}

trait Repository[T] {
  val root : Root

  protected def getReference(relationshipType : RelationshipType) =
    root.getReference(relationshipType).getOrElse(root.createReference(relationshipType)).getEndNode

  def create(data : Map[String, Any]) : T
}

class Root(private var database: GraphDatabaseService) {
  private val root = database.getReferenceNode

  def getReference(relationshipType : RelationshipType) = Option(
    root.getSingleRelationship(relationshipType, Direction.OUTGOING))

  def createReference(relationshipType : RelationshipType) =
    root.createRelationshipTo(database.createNode, relationshipType)

  def createNode = database.createNode
}
