package org.megrez.server

import org.neo4j.graphdb.Traverser.Order
import scala.collection.JavaConversions._
import org.neo4j.graphdb._

trait Neo4jHelper {
  def cleanData {
    Neo4jServer.exec {
      neo => {
        allPipelineNodes.foreach {
          node: Node => {
            node.getRelationships.foreach(_.delete)
            node.delete
          }
        }
      }
    }
  }

  def allPipelineNodes = Neo4jServer.exec {
      neo => {
        val rootRelationship: Relationship = neo.getReferenceNode.getSingleRelationship(DynamicRelationshipType.withName("PIPELINES"), Direction.OUTGOING)
        val pipelinesNode: Node = rootRelationship.getEndNode
        val pipelineTraverse = pipelinesNode.traverse(Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE,
          ReturnableEvaluator.ALL_BUT_START_NODE, DynamicRelationshipType.withName("PIPELINE"), Direction.OUTGOING)
        pipelineTraverse.getAllNodes
      }
    }

  def cleanupDatabase {
    cleanData
    Neo4jServer.shutdown
  }
}