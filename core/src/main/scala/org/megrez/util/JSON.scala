package org.megrez.util

import collection.mutable.HashMap
import org.megrez._
import org.megrez.task.CommandLineTask
import vcs.{Git, Subversion}

object JSON {
  private val JsonParser = scala.util.parsing.json.JSON

  def read[T](json: String)(implicit jsObject: JsonSerializer[T]): T = JsonParser.parseFull(json) match {
    case Some(json: Map[String, Any]) => jsObject.read(json)
    case _ => throw new Exception
  }

  def write[T](resource: T)(implicit jsObject: JsonSerializer[T]): String = {
    def toJson(obj: Map[String, Any]): String =
      obj.map(keyValue => "\"" + keyValue._1 + "\":" +
              (keyValue._2 match {
                case string: String => "\"" + string + "\""
                case set: Set[String] => set.map("\"" + _ + "\"").mkString("[", ",", "]")
                case list: List[Map[String, Any]] => list.map(toJson).mkString("[", ",", "]")
                case map: Map[String, Any] => toJson(map)
              }
                      )
        ).mkString("{", ",", "}")

    toJson(jsObject.write(resource))
  }

  private def readObject[T](json: Map[String, Any])(implicit jsObject: JsonSerializer[T]): T = jsObject.read(json)

  private def writeObject[T](resource: T)(implicit jsObject: JsonSerializer[T]): Map[String, Any] = jsObject.write(resource)

  trait JsonSerializer[T] extends Serializer[T, Map[String, Any]]

  trait TypeBasedSerializer[T <: AnyRef] extends JsonSerializer[T] {
    private val parsers = HashMap[String, JsonSerializer[_ <: T]]()
    private val writers = HashMap[Class[_], JsonSerializer[_ <: T]]()

    def register[U <: T](jsonType: String)(implicit m: Manifest[U], handler: JsonSerializer[U]) {
      parsers.put(jsonType, handler)
      writers.put(m.erasure, handler)
    }

    def read(representation: Map[String, Any]): T =
      parsers.get(representation / "type") match {
        case Some(parser) =>
          parser.read(representation)
        case None => throw new Exception()
      }

    def write(resource: T): Map[String, Any] =
      writers.get(resource.getClass) match {
        case Some(writer) => writer.asInstanceOf[JsonSerializer[T]].write(resource)
        case None => throw new Exception()
      }
  }

  implicit object ChangeSourceSerializer extends TypeBasedSerializer[ChangeSource]

  implicit object TaskSerializer extends TypeBasedSerializer[Task]

  implicit object MaterialSerializer extends JsonSerializer[Material] {
    def read(json: Map[String, Any]) = new Material(readObject[ChangeSource](json), json / "dest")

    def write(material: Material): Map[String, Any] = writeObject[ChangeSource](material.source) ++ Map("dest" -> material.destination)
  }

  implicit object SubversionSerializer extends JsonSerializer[Subversion] {
    def read(representation: Map[String, Any]) = new Subversion(representation / "url")

    def write(resource: Subversion) = Map("type" -> "svn", "url" -> resource.url)
  }

  implicit object GitSerializer extends JsonSerializer[Git] {
    def read(representation: Map[String, Any]) = new Git(representation / "url")

    def write(resource: Git) = Map("type" -> "git", "url" -> resource.url)
  }

  ChangeSourceSerializer.register[Subversion]("svn")
  ChangeSourceSerializer.register[Git]("git")

  implicit object CommandLineTaskSerializer extends JsonSerializer[CommandLineTask] {
    def read(representation: Map[String, Any]) = new CommandLineTask(representation / "command")

    def write(resource: CommandLineTask) = Map("type" -> "cmd", "command" -> resource.command)
  }

  TaskSerializer.register[CommandLineTask]("cmd")

  implicit object JobSerializer extends JsonSerializer[Job] {
    def read(json: Map[String, Any]) = new Job(json / "name", json / ("resources", _.toSet), json > ("tasks", readObject[Task](_)))

    def write(job: Job) = Map("name" -> job.name, "resources" -> job.resources, "tasks" -> job.tasks.map(writeObject(_)))
  }

  implicit object StageSerializer extends JsonSerializer[Pipeline.Stage] {
    def read(json: Map[String, Any]) = new Pipeline.Stage(json / "name", (json > ("jobs", readObject[Job](_))).toSet)

    def write(stage: Pipeline.Stage) = Map("name" -> stage.name, "jobs" -> stage.jobs.map(writeObject(_)).toList)
  }

  implicit def map2Json(json: Map[String, Any]): JsonHelper = new JsonHelper(json)

  class JsonHelper(val json: Map[String, Any]) {
    def /[T](name: String): T = {
      json(name) match {
        case result: T => result
        case _ => throw new Exception()
      }
    }

    //get value of json(Map) by the value(Map) of it's key
    def >[V](name: String, map: Map[String, Any] => V) = {
      json(name) match {
        case list: List[Map[String, Any]] => list.map(map)
        case _ => throw new Exception()
      }
    }

    def /[V](name: String, map: List[String] => V) = {
      json(name) match {
        case list: List[String] => map(list)
        case _ => throw new Exception()
      }
    }
  }
}