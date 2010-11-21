package org.megrez.server.data

import org.neo4j.graphdb._

class Pipeline private (val node: Node) extends Entity {
  val name = property(Pipeline.name)
  val stages = list(Pipeline.stags)
}

object Pipeline extends Repository[Pipeline] with Metadata[Pipeline] {
  import DynamicRelationshipType._

  val name = property[String]("name")
  val stags = list("stages", Stage, withName("START_WITH"), withName("NEXT_STAGE"))

  val referenceType = DynamicRelationshipType.withName("PIPELINES")

  private def activePipeline = DynamicRelationshipType.withName("ACTIVE_PIPELINE")

  def create(data: Map[String, Any]) = transaction {Pipeline(reference.createRelationshipTo(graph.createNode, activePipeline).getEndNode, data)}

  def apply(node: Node) = new Pipeline(node)
}