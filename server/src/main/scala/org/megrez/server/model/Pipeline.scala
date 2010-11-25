package org.megrez.server.model

import data.{Repository, Entity}
import org.neo4j.graphdb.DynamicRelationshipType._
import org.neo4j.graphdb.Node

class Pipeline private(val node: Node) extends Entity with org.megrez.model.Pipeline {
  val name = read(Pipeline.name)
  val materials = read(Pipeline.materials)
  val stages = read(Pipeline.stages)
}

object Pipeline extends Repository[Pipeline] {
  val root = withName("PIPELINES")
  val entity = withName("ACTIVE_PIPELINE")

  val name = property[String]("name")
  val materials = set("materials", Material, withName("TRIGGER FROM"))
  val stages = list("stages", Stage, withName("START_WITH"))

  def apply(node: Node) = new Pipeline(node)
}