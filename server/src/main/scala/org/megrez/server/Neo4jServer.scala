package org.megrez.server

import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.kernel.EmbeddedGraphDatabase

/**
 * Wrapper around a singleton instance of Neo4j embedded server.
 */
object Neo4jServer {
  private var neo: GraphDatabaseService = null

  def startup() {
    neo = new EmbeddedGraphDatabase("database/megrez")

    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run() {
        Neo4jServer.shutdown
      }
    })
  }

  /**
   * Do a clean shutdown of Neo4j.
   */
  def shutdown {
    synchronized {
      if (neo != null) neo.shutdown
      neo = null
    }
  }

  /**
   * Execute instructions within a Neo4j transaction; rollback if exception is raised and
   * commit otherwise; and return the return value of the operation.
   */
  def exec[T <: Any](operation: GraphDatabaseService => T): T = {
    val tx = {
      if (neo == null) startup
      neo.beginTx
    }
    try {
      val ret = operation(neo)
      tx.success
      return ret
    } finally {
      tx.finish
    }
  }
}
