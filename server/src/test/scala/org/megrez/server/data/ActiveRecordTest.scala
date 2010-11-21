package org.megrez.server.data

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{Spec, BeforeAndAfterEach}
import java.io.File
import org.neo4j.kernel.EmbeddedGraphDatabase
import org.neo4j.graphdb.{DynamicRelationshipType, Direction, GraphDatabaseService}

class ActiveRecordTest extends Spec with ShouldMatchers with BeforeAndAfterEach {
  describe("Active Record like layer on top of neo4j") {
    import scala.collection.JavaConversions._

    it("should create stage with details") {      
      val stage = transaction {Stage(neo.createNode, Map("name"->"ut", "jobs" -> List(Map("name" -> "unit-test"))))}
      stage.name() should equal("ut")
      stage.jobs() should have size(1)
    }

    it("created pipeline should be connected to root") {
      val pipeline = Pipeline.create(Map("name" -> "name", "stages" -> List()))
      val pipelines = neo.getReferenceNode.getSingleRelationship(DynamicRelationshipType.withName("PIPELINES"), Direction.OUTGOING).getEndNode
      val relationships = pipelines.getRelationships(DynamicRelationshipType.withName("ACTIVE_PIPELINE"), Direction.OUTGOING).toList
      relationships should have size(1)            
    }

    it("should create pipeline with details") {
        val pipeline = Pipeline.create(Map("name" -> "name", "stages" -> List(Map("name" -> "ut"))))
        pipeline.name() should equal("name")
        pipeline.stages() should have size(1)
        pipeline.stages().head.name() should equal("ut")
    }
  }

  val root: File = new File(System.getProperty("user.dir"), "database/megrez")
  var neo: GraphDatabaseService = _

  override def beforeEach() {
    root.mkdirs
    neo = new EmbeddedGraphDatabase(root.getAbsolutePath)
    Graph.from(neo)
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