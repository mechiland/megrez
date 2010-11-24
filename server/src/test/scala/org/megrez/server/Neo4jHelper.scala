package org.megrez.server

import data.Graph
import org.neo4j.graphdb.Traverser.Order
import scala.collection.JavaConversions._
import org.neo4j.graphdb._
import org.neo4j.kernel.EmbeddedGraphDatabase
import java.io.File

trait Neo4jHelper {
  def allPipelineNodes = transaction {
    val rootRelationship: Relationship = neo.getReferenceNode.getSingleRelationship(DynamicRelationshipType.withName("PIPELINES"), Direction.OUTGOING)
    val pipelinesNode: Node = rootRelationship.getEndNode
    val pipelineTraverse = pipelinesNode.traverse(Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE,
      ReturnableEvaluator.ALL_BUT_START_NODE, DynamicRelationshipType.withName("ACTIVE_PIPELINE"), Direction.OUTGOING)
    pipelineTraverse.getAllNodes
  }

  var neo: GraphDatabaseService = _
  val dbRoot: File = new File(System.getProperty("user.dir"), "database/test")

  def shutdownDB {
    neo.shutdown
    delete(dbRoot)
  }

  private def delete(file: File) {
    file.listFiles.foreach {
      file =>
        if (file.isDirectory) delete(file) else file.delete
    }
    file.delete
  }

  def startDB {
    dbRoot.mkdirs
    neo = new EmbeddedGraphDatabase(dbRoot.getAbsolutePath)
    Graph.of(neo)
  }

  def transaction[T](operation: => T): T = {
    val tx = neo.beginTx
    try {
      val result = operation
      tx.success
      result
    } finally {
      tx.finish
    }
  }
}