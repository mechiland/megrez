package org.megrez.io

import collection.mutable.HashMap
import collection.immutable.List
import org.megrez._
import scala.Option
import task.CommandLineTask
import vcs.{Git, Subversion}

trait JSON[T] extends Reader[T, Any] with Write[T, Any] {
  def read(representation: Any): T = readJson(asJson(representation))

  def write(resource: T):String = writeJson(resource)

  protected def readJson(json: Map[Any, Any]): T

  protected def writeJson(resource: T):String

  protected def asJson(json: Any) =
    json match {
      case json: Map[Any, Any] => json
      case _ => throw new Exception
    }

  protected def asResource(resource: T) =
    resource match {
      case resource: JobAssignment => resource
      case resource: Material => resource
      case resource: Job => resource
      case resource: Task => resource
      case resource: JobFailed => resource
      case resource: JobCompleted => resource
      case _ => throw new Exception
    }
}

trait PluggableJSON[T] extends JSON[T] {
  import JSON._
  private val parser = HashMap[String, Map[Any, Any] => T]()

  def register(jsonType: String, factory: Map[Any, Any] => T) {
    parser.put(jsonType, factory)
  }

  override protected def writeJson(source: T):String = {
    source match {
      case svn: Subversion =>return List(formatItem("type","svn"), formatItem("url", svn.url)).mkString(", ")
      case git: Git =>return List(formatItem("type","git"), formatItem("url",git.url)).mkString(", ")
      case _ => ""
    }
  }

  override protected def readJson(json: Map[Any, Any]) = parser(json / "type")(json)
}

object JSON {
  type Json = Map[Any, Any]

  def read[T](json: Any)(implicit resource: JSON[T]): T = resource.read(json)

  def write[T](message: T)(implicit resource: JSON[T]):String = resource.write(message)

  def formatGroup(key:String , value: String): String = {
    List(key, value).mkString("{\"", "\" : \"", "\"}")
  }
  def formatItem(key:String , value: String): String = {
    List(key, value).mkString("\"", "\" : \"", "\"")
  }
  def formatTitle(title:String): String = {
    "\""+title+"\" : "
  }

  implicit object ChangeSourceJson extends PluggableJSON[ChangeSource] {
    private val workSetParsers = HashMap[String, Json => Option[Any]]()

    def registerWorkSet(jsonType: String, factory: Json => Option[Any]) {
      workSetParsers.put(jsonType, factory)
    }

    def writeWorkSet(source: ChangeSource):String = {
      source match {
        case svn: Subversion => return formatGroup("revision",workSetParsers("svn").toString)
        case svn: Git => return formatGroup("revision",workSetParsers("git").toString)
        case _ => return ""
      }
    }

    def readWorkSet(jsonType: String, json: Json) = workSetParsers(jsonType)(json)
  }

  implicit object MaterialJson extends JSON[Material] {
    override protected def writeJson(material: Material):String = {
    return formatTitle("materials") + List(formatTitle("material") + List(ChangeSourceJson.write(material.source), formatItem("dest",material.destination)).mkString("{", ", ", "}"), formatTitle("workset") + ChangeSourceJson.writeWorkSet(material.source)).mkString("[{", ", ", "}]")
    }

    override protected def readJson(json: Json) = new Material(JSON.read[ChangeSource](json), json / "dest")
  }

  implicit object TaskJson extends PluggableJSON[Task] {
    override protected def writeJson(task: Task):String = {
      task match {
        case task: CommandLineTask =>return formatTitle("tasks") + List(formatItem("type","cmd"), formatItem("command", task.command)).mkString("[{", ", ", "}]")
        case _ =>return ""
      }
    }
  }

  implicit object JobJson extends JSON[Job] {
    override protected def readJson(json: Json) = new Job(json / "name", json > ("tasks", task => JSON.read[Task](task)))

    override protected def writeJson(job: Job):String = {
      var taskList: String = ""
      job.tasks.foreach(task => taskList += JSON.write[Task](task))
      return formatTitle("job")+List(formatItem("name",job.name), taskList).mkString("{", ", ", "}")
    }
  }

  implicit object PipelineJson extends JSON[Pipeline] {
    override protected def readJson(json: Json) = new Pipeline(json / "name",
      (json > ("materials", material => JSON.read[Material](material))).toSet, List[Pipeline.Stage]())

    override protected def writeJson(pipeline: Pipeline):String = {
     return formatItem("pipeline" , pipeline.name)
    }
  }

  implicit object JobAssignmentJson extends JSON[JobAssignment] {
    protected override def readJson(json: Json) =
      JobAssignment(json / "pipeline", (json > ("materials", material =>
        JSON.read[Material](material("material")) ->
                ChangeSourceJson.readWorkSet(asJson(material("material")) / "type", asJson(material("workset")))
              )).toMap, JSON.read[Job](json / "job"))

    protected override def writeJson(message: JobAssignment):String = {
      var materialList: String = ""
      val materials: Map[Material, Option[Any]] = message.materials
      materials.foreach({
        case (material, optionOfMaterial) => materialList += JSON.write[Material](material)
        case _ => ""
      })
     return List(formatItem("pipeline", message.pipeline), materialList, JSON.write[Job](message.job)).mkString("{", ", ", "}")
    }
  }

  ChangeSourceJson.register("svn", json => new Subversion(json / "url"))

  ChangeSourceJson.registerWorkSet("svn", json => Some(Integer.parseInt(json / "revision")))

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