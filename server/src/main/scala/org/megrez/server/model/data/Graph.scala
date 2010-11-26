package org.megrez.server.model.data

import collection.mutable.HashSet
import org.neo4j.graphdb._

object Graph {
  private var graph: GraphDatabaseService = null
  private var meta = HashSet[Meta[_ <: Entity]]()

  def of(graph: GraphDatabaseService): this.type = {
    this.graph = graph
    this
  }

  def consistOf(meta: Meta[_ <: Entity]*): this.type = {
    for (m <- meta) {
      this.meta.add(m)
      m.graph = graph
    }
    this
  }

  class NodeHelper(val node: Node) {
    def update(operation: Node => Unit) {
      val transaction = node.getGraphDatabase.beginTx
      try {
        operation(node)
        transaction.success
      } finally {
        transaction.finish
      }
    }
  }

  class GraphHelper(val graph: GraphDatabaseService) {
    def update[T](operation: GraphDatabaseService => T):T ={
      val transaction = graph.beginTx
      try {
        val result = operation(graph)
        transaction.success
        result
      } finally {
        transaction.finish
      }
    }
  }

  implicit def node2NodeHelper(node: Node) = new NodeHelper(node)
  implicit def graph2GraphHelper(graph: GraphDatabaseService) = new GraphHelper(graph)
  implicit def returnableEvaluator(func: TraversalPosition => Boolean) = new ReturnableEvaluator {
    def isReturnableNode(traversalPosition: TraversalPosition ) = func(traversalPosition)
  }

  def last(name : String) = returnableEvaluator(position => !position.currentNode.hasRelationship(DynamicRelationshipType.withName(name), Direction.OUTGOING))
}

