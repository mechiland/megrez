package org.megrez.server.data

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{Spec, BeforeAndAfterEach}
import java.io.File
import org.neo4j.kernel.EmbeddedGraphDatabase
import org.neo4j.graphdb.{Direction, GraphDatabaseService}

class PipelineTest extends Spec with ShouldMatchers with BeforeAndAfterEach {
  import scala.collection.JavaConversions._

  describe("Pipeline repository") {
    it("should create a reference node") {
      transaction {
        val root = new Root(neo)
        new Pipelines(root)
      }

      val references = neo.getReferenceNode.getRelationships(Relationships.pipelines, Direction.OUTGOING).toList
      references should have size (1)
    }

    it("should create a pipeline with name") {
      val pipeline = transaction {
        val pipelines = new Pipelines(new Root(neo))
        pipelines.create(Map("name" -> "name"))
      }
      pipeline.name() should equal("name")
    }    
  }

  val root: File = new File(System.getProperty("user.dir"), "database/megrez")
  var neo: GraphDatabaseService = _

  override def beforeEach() {
    root.mkdirs
    neo = new EmbeddedGraphDatabase(root.getAbsolutePath)
  }

  override def afterEach() {
    neo.shutdown
    delete(root)
  }

  private def delete(file: File) {
    file.listFiles.foreach {
      file =>
        if (file.isDirectory) delete(file) else file.delete
    }
    file.delete
  }

  def transaction[T](operation: => T):T= {
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