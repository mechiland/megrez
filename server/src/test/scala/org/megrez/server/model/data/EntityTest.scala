package org.megrez.server.model.data

import org.scalatest.matchers.ShouldMatchers
import org.megrez.server.{IoSupport, Neo4JSupport}
import org.neo4j.graphdb.{DynamicRelationshipType, Node}
import org.scalatest.{BeforeAndAfterAll, Spec}

class EntityTest extends Spec with ShouldMatchers with BeforeAndAfterAll with IoSupport with Neo4JSupport {
  describe("Properties") {
    it("should create model with primitive value") {
      val entity = PrimitiveValues(Map("string" -> "string", "num" -> 5))
      entity.string should equal("string")
      entity.num should equal(5)
    }

    it("should create model with array of primative value") {
      val entity = PrimitiveArrays(Map("string" -> List("string", "array"), "num" -> List(5, 6, 7)))
      entity.string should equal(Array("string", "array"))
      entity.num should equal(Array(5, 6, 7))
    }
  }

  describe("References") {
    it("should create model with refernece to other model") {
      val entity = One(Map("name" -> "name", "another" -> Map("name" -> "name")))
      entity.name should equal("name")
      entity.another.name should equal("name")
    }

    it("should create model with reference instance") {
      val another = Another(Map("name" -> "name"))
      val entity = One(Map("name" -> "name", "another" -> another))
      entity.name should equal("name")
      entity.another.name should equal("name")
    }

    it("should create model with list references") {
      val entity = ListAnother(Map("name" -> "name", "others" -> List(Map("name" -> "name"))))
      entity.others should have size(1)
      entity.others.head.name should equal("name")
    }

    it("should create model with set references") {
      val entity = SetAnother(Map("name" -> "name", "others" -> List(Map("name" -> "name"))))
      entity.others should have size (1)
      entity.others.head.name should equal("name")
    }
  }

  describe("Pluggable") {
    it("should create pluggable type") {
      val entity = PluggableEntity(Map("type" -> "plugin", "name" -> "name"))
      entity match {
        case plugin : PluginEntity =>
          plugin.name should equal("name")
        case _ => fail
      }
    }
  }

  override def beforeAll() {
    Neo4J.start
    Graph.of(neo).consistOf(PrimitiveValues, PrimitiveArrays, One, Another, ListAnother, SetAnother,
      PluggableEntity, PluginEntity)
  }

  override def afterAll() {
    Neo4J.shutdown
  }

  class PrimitiveValues private(val node: Node) extends Entity {
    val string = read(PrimitiveValues.string)
    val num = read(PrimitiveValues.num)
  }

  object PrimitiveValues extends Meta[PrimitiveValues] {
    val string = property[String]("string")
    val num = property[Int]("num")

    def apply(node: Node) = new PrimitiveValues(node)
  }

  class PrimitiveArrays private(val node: Node) extends Entity {
    val string = read(PrimitiveArrays.string)
    val num = read(PrimitiveArrays.num)
  }

  object PrimitiveArrays extends Meta[PrimitiveArrays] {
    val string = property[Array[String]]("string")
    val num = property[Array[Int]]("num")

    def apply(node: Node) = new PrimitiveArrays(node)
  }

  class One private(val node: Node) extends Entity {
    val name = read(One.name)
    val another = read(One.another)
  }

  object One extends Meta[One] {
    val name = property[String]("name")
    val another = reference("another", Another, DynamicRelationshipType.withName("HAS"))

    def apply(node: Node) = new One(node)
  }

  class Another private(val node: Node) extends Entity {
    val name = read(Another.name)
  }

  object Another extends Meta[Another] {
    val name = property[String]("name")

    def apply(node: Node) = new Another(node)
  }

  class ListAnother private(val node: Node) extends Entity {
    val others = read(ListAnother.others)
  }

  object ListAnother extends Meta[ListAnother] {
    val others = list("others", Another, DynamicRelationshipType.withName("HAS"))

    def apply(node: Node) = new ListAnother(node)
  }

  class SetAnother private(val node: Node) extends Entity {
    val others = read(SetAnother.others)
  }

  object SetAnother extends Meta[SetAnother] {
    val others = set("others", Another, DynamicRelationshipType.withName("HAS"))

    def apply(node: Node) = new SetAnother(node)
  }

  abstract class PluggableEntity extends Entity  

  object PluggableEntity extends Pluggable[PluggableEntity]

  class PluginEntity private (val node : Node) extends PluggableEntity {
    val name = read(PluginEntity.name)
  }

  object PluginEntity extends Plugin(PluggableEntity, "plugin") {
    val name = property[String]("name")
    
    def apply(node : Node) = new PluginEntity(node)
  }
}