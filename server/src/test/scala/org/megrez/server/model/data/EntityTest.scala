package org.megrez.server.model.data

import org.scalatest.matchers.ShouldMatchers
import org.megrez.server.{IoSupport, Neo4JSupport}
import org.scalatest.{BeforeAndAfterEach, Spec}
import org.neo4j.graphdb.Node

class EntityTest extends Spec with ShouldMatchers with BeforeAndAfterEach with IoSupport with Neo4JSupport {
  describe("Properties") {
    it("should create model with primitive value") {
      val entity = PrimitiveValues(Map("string" -> "string", "num" -> 5))
      entity.string() should equal(Some("string"))
      entity.num() should equal(Some(5))
    }

    it("should create model with array of primative value") {
      val entity = PrimitiveArrays(Map("string" -> List("string", "array"), "num" -> List(5, 6, 7)))
      entity.string() match {
        case Some(array) =>
          array should equal(Array("string", "array"))
        case _ => fail
      }
      entity.num() match {
        case Some(array) =>
          array should equal(Array(5, 6, 7))
        case _ => fail
      }
    }
  }

  describe("References") {
    it("should create model with refernece to other model")  {
      
    }
  }

  override def beforeEach() {
    Neo4J.start
    Graph.of(neo).consistOf(PrimitiveValues, PrimitiveArrays)
  }

  override def afterEach() {
    Neo4J.shutdown
  }

  class PrimitiveValues private(val node: Node) extends Entity {
    val string = accessor(PrimitiveValues.string)
    val num = accessor(PrimitiveValues.num)
  }

  object PrimitiveValues extends Meta[PrimitiveValues] {
    val string = property[String]("string")
    val num = property[Int]("num")

    def apply(node: Node) = new PrimitiveValues(node)
  }

  class PrimitiveArrays private(val node: Node) extends Entity {
    val string = accessor(PrimitiveArrays.string)
    val num = accessor(PrimitiveArrays.num)
  }

  object PrimitiveArrays extends Meta[PrimitiveArrays] {
    val string = property[Array[String]]("string")
    val num = property[Array[Int]]("num")

    def apply(node: Node) = new PrimitiveArrays(node)
  }

}