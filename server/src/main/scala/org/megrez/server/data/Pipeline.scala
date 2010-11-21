package org.megrez.server.data

import org.neo4j.graphdb._

class Pipeline(val node: Node) extends Entity {
  val name = property(Pipeline.name)
  val stages = list(Pipeline.stags)

  def updateAttributes(data: Map[String, Any]) {
    transaction {
      name(data("name").asInstanceOf[String])
      data("stages").asInstanceOf[List[Map[String, Any]]].foreach {
        stageData =>
          val stage = Stage(graph.createNode)
          stage.updateAttributes(stageData)
          stages << stage
      }
    }
  }
}

object Pipeline extends Repository[Pipeline] with Metadata[Pipeline] {
  import DynamicRelationshipType._

  val name = property[String]("name")
  val stags = list(Stage, withName("START_WITH"), withName("NEXT_STAGE"))

  val referenceType = DynamicRelationshipType.withName("PIPELINES")

  private def activePipeline = DynamicRelationshipType.withName("ACTIVE_PIPELINE")

  def create(data: Map[String, Any]) =
    transaction {
      val pipeline = Pipeline(reference.createRelationshipTo(graph.createNode, activePipeline).getEndNode)
      pipeline.updateAttributes(data)
      pipeline
    }

  def apply(node: Node) = new Pipeline(node)
}