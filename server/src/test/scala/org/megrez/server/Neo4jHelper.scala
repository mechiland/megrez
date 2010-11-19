package org.megrez.server

import org.neo4j.graphdb.Traverser.Order
import scala.collection.JavaConversions._
import org.neo4j.graphdb._

trait Neo4jHelper {
  def cleanupDatabase {
    println("starts to cleanup database")
    Neo4jServer.exec {
      neo => {
        val rootRelationship : Relationship = neo.getReferenceNode.getSingleRelationship(DynamicRelationshipType.withName("PIPELINES"), Direction.OUTGOING)
        val pipelinesNode : Node = rootRelationship.getEndNode
        val pipelineTraverse = pipelinesNode.traverse(Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE,
          ReturnableEvaluator.ALL_BUT_START_NODE, DynamicRelationshipType.withName("PIPELINE"), Direction.OUTGOING)
        val nodes = pipelineTraverse.getAllNodes
        println("nodes amount after test: " + nodes.size)
        nodes.foreach {
          node: Node => {
            println("node name: " + node.getProperty("name"))
            node.getRelationships.foreach(_.delete)
            node.delete
          }
        }
        println("nodes amount after cleanup: " + nodes.size)
      }
    }
    Neo4jServer.shutdown
  }
}