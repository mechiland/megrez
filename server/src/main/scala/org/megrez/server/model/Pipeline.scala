package org.megrez.server.model

import data.{Repository, Entity}
import org.neo4j.graphdb.DynamicRelationshipType._
import org.neo4j.graphdb.{Node, DynamicRelationshipType}

//class Pipeline private(val node: Node) extends Entity with org.megrez.model.Pipeline {
//}
//
//object Pipeline extends Repository[Pipeline] {
//  val root = withName("PIPELINES")
//  val entity = withName("ACTIVE_PIPELINE")
//
//  val name = property[String]("name")
//  //  val stages = list("stages", )
//
//  def apply(node: Node) = new Pipeline(node)
//}