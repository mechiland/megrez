package org.megrez.io

import org.megrez.vcs.Subversion
import collection.mutable.HashMap
import org.megrez._
import java.util.UUID
import task.CommandLineTask

trait JSON[T] extends Reader[T, Any] {
  def read(representation: Any): T = readJson(asJson(representation))

  protected def readJson(json: Map[Any, Any]): T

  protected def asJson(json: Any) =
    json match {
      case json: Map[Any, Any] => json
      case _ => throw new Exception
    }
}

trait PluggableJSON[T] extends JSON[T] {
  import JSON._
  private val parser = HashMap[String, Map[Any, Any] => T]()

  def register(jsonType: String, factory: Map[Any, Any] => T) {
    parser.put(jsonType, factory)
  }

  override protected def readJson(json: Map[Any, Any]) = parser(json / "type")(json)
}

object JSON {
  type Json = Map[Any, Any]

  def read[T](json: Any)(implicit resource: JSON[T]): T = resource.read(json)

  implicit object MaterialJson extends PluggableJSON[Material] {
    private val workSetParsers = HashMap[String, Json => Option[Any]]()

    def registerWorkSet(jsonType: String, factory: Json => Option[Any]) {
      workSetParsers.put(jsonType, factory)
    }

    def readWorkSet(jsonType: String, json: Json) = workSetParsers(jsonType)(json)
  }

  implicit object TaskJson extends PluggableJSON[Task]

  implicit object JobJson extends JSON[Job] {
    override protected def readJson(json: Json) = new Job(json / "name", json > ("tasks", task => JSON.read[Task](task)))
  }

  implicit object PipelineJson extends JSON[Pipeline] {
    override protected def readJson(json: Json) = new Pipeline(json / "name",
      (json > ("materials", material => JSON.read[Material](material))).toSet, List[Pipeline.Stage]())
  }

  implicit object JobAssignmentJson extends JSON[JobAssignment] {
    protected override def readJson(json: Json) =
      JobAssignment(json / "pipeline", (json > ("materials", material =>
        JSON.read[Material](material("material")) ->
                MaterialJson.readWorkSet(asJson(material("material")) / "type", asJson(material("workset")))
              )).toMap, JSON.read[Job](json / "job"))
  }

  MaterialJson.register("svn", json => new Subversion(json / "url"))

  MaterialJson.registerWorkSet("svn", json => Some(Integer.parseInt(json / "revision")))

  TaskJson.register("cmd", json => new CommandLineTask(json / "command"))

  implicit def map2Json(json: Json): JsonHelper = new JsonHelper(json)

  class JsonHelper(val json: Json) {
    def /[T](name: Any): T = {
      json(name) match {
        case result: T => result
        case _ => throw new Exception()
      }
    }

    def >[V](name: Any, map: Json => V) = {
      json(name) match {
        case list: List[Json] =>
          list.map(map)
        case _ => throw new Exception()
      }
    }
  }
}