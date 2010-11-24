package org.megrez.server

import java.io.File
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.kernel.EmbeddedGraphDatabase

trait IoSupport {
  def delete(file: File) {
    file.listFiles.foreach {
      file =>
        if (file.isDirectory) delete(file) else file.delete
    }
    file.delete
  }
}

trait Neo4JSupport { self : IoSupport =>
  private val database = new File(System.getProperty("user.dir"), "database")

  protected var neo : GraphDatabaseService = null

  object Neo4J {
    def start() {
      database.mkdirs
      neo = new EmbeddedGraphDatabase(database.getAbsolutePath)
    }

    def shutdown() {
      neo.shutdown
      delete(database)
    }    
  }
}