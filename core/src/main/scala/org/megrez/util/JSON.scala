package org.megrez.util

import collection.mutable.HashMap
import org.megrez._
import task.{AntTask, CommandLineTask}
import vcs.{Git, Subversion}
import java.net.{URLDecoder, URLEncoder}

object JSON {
  private val JsonParser = scala.util.parsing.json.JSON

  def read[T](json: String)(implicit jsObject: JsonSerializer[T]): T = JsonParser.parseFull(json) match {
    case Some(json: Map[String, Any]) => jsObject.read(json)
    case _ => throw new Exception
  }

  def write[T](resource: T)(implicit jsObject: JsonSerializer[T]): String = {
    def toJson(obj: Map[String, Any]): String =
      obj.filter(keyValue => keyValue._2 != null).map(keyValue => "\"" + keyValue._1 + "\":" +
              (keyValue._2 match {
                case string: String => "\"" + string + "\""
                case set: Set[String] => set.map("\"" + _ + "\"").mkString("[", ",", "]")
                case list: List[Map[String, Any]] => list.map(toJson).mkString("[", ",", "]")
                case int: Int => int
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

  implicit object ChangeSourceSerializer extends TypeBasedSerializer[ChangeSource] {
    private val worksets = HashMap[Class[_], (Map[String, Any] => Option[Any], Option[Any] => Map[String, Any])]()

    def register[T <: ChangeSource](parser: Map[String, Any] => Option[Any], writer: Option[Any] => Map[String, Any])(implicit m: Manifest[T]) {
      worksets.put(m.erasure, (parser, writer))
    }

    def readWorkset(source: ChangeSource, json: Map[String, Any]) = worksets.get(source.getClass) match {
      case Some(Pair(parser, _)) => parser(json)
      case _ => throw new Exception()
    }

    def writeWorkset(source: ChangeSource, workset: Option[Any]) = worksets.get(source.getClass) match {
      case Some(Pair(_, writer)) => writer(workset)
      case _ => throw new Exception()
    }
  }

  implicit object TaskSerializer extends TypeBasedSerializer[Task]

  implicit object MaterialSerializer extends JsonSerializer[Material] {
    def read(json: Map[String, Any]) = new Material(readObject[ChangeSource](json), json / "dest")

    def write(material: Material): Map[String, Any] = writeObject[ChangeSource](material.source) ++ Map("dest" -> material.destination)
  }

  implicit object SubversionSerializer extends JsonSerializer[Subversion] {
    def read(json: Map[String, Any]) = new Subversion(json / "url")

    def write(resource: Subversion) = Map("type" -> "svn", "url" -> resource.url)

    private val readWorkset: Map[String, Any] => Option[Any] = json => Some(json / "revision")
    private val writeWorkset: Option[Any] => Map[String, Any] = revision => revision match {
      case Some(revision: Int) => Map("revision" -> revision)
      case _ => throw new Exception()
    }

    ChangeSourceSerializer.register[Subversion](readWorkset, writeWorkset)
  }

  implicit object GitSerializer extends JsonSerializer[Git] {
    def read(representation: Map[String, Any]) = new Git(representation / "url")

    def write(resource: Git) = Map("type" -> "git", "url" -> resource.url)

    private val readWorkset: Map[String, Any] => Option[Any] = json => Some(json / "commit")
    private val writeWorkset: Option[Any] => Map[String, Any] = commit => commit match {
      case Some(commit: String) => Map("commit" -> commit)
      case _ => throw new Exception()
    }

    ChangeSourceSerializer.register[Git](readWorkset, writeWorkset)
  }

  ChangeSourceSerializer.register[Subversion]("svn")
  ChangeSourceSerializer.register[Git]("git")

  implicit object CommandLineTaskSerializer extends JsonSerializer[CommandLineTask] {
    def read(representation: Map[String, Any]) = new CommandLineTask(representation / "command")

    def write(resource: CommandLineTask) = Map("type" -> "cmd", "command" -> resource.command)
  }

  TaskSerializer.register[CommandLineTask]("cmd")

  implicit object AntTaskSerializer extends JsonSerializer[AntTask] {
    def read(json: Map[String, Any]) = new AntTask(json /? ("target", null), json /? ("buildfile", null))

    def write(resource: AntTask) = Map("type" -> "ant", "target" -> resource.target, "buildfile" -> resource.buildfile)
  }

  TaskSerializer.register[AntTask]("ant")

  implicit object JobSerializer extends JsonSerializer[Job] {
    def read(json: Map[String, Any]) = new Job(json / "name", json / ("resources", _.toSet), json > ("tasks", readObject[Task](_)), json > ("artifacts", readObject[Artifact](_)))

    def write(job: Job) = {
      if (job.artifacts.isEmpty)
        Map("name" -> job.name, "resources" -> job.resources, "tasks" -> job.tasks.map(writeObject(_)))
      else
        Map("name" -> job.name, "resources" -> job.resources, "tasks" -> job.tasks.map(writeObject(_)), "artifacts" -> job.artifacts.map(writeObject[Artifact](_)))
    }
  }

  implicit object StageSerializer extends JsonSerializer[Pipeline.Stage] {
    def read(json: Map[String, Any]) = new Pipeline.Stage(json / "name", (json > ("jobs", readObject[Job](_))).toSet)

    def write(stage: Pipeline.Stage) = Map("name" -> stage.name, "jobs" -> stage.jobs.map(writeObject(_)).toList)
  }

  implicit object PipelineSerializer extends JsonSerializer[Pipeline] {
    def read(json: Map[String, Any]) = new Pipeline(json / "name", (json > ("materials", readObject[Material](_))).toSet, json > ("stages", readObject[Pipeline.Stage](_)))

    def write(pipeline: Pipeline) = Map("name" -> pipeline.name, "materials" -> pipeline.materials.map(writeObject(_)).toList, "stages" -> pipeline.stages.map(writeObject(_)))
  }

  implicit object ArtifactSerializer extends JsonSerializer[Artifact] {
    def read(json: Map[String, Any]) = new Artifact(json / "path", json / ("tags", _.toSet))

    def write(artifact: Artifact) = Map("path" -> artifact.path, "tags" -> artifact.tags)
  }

  implicit object AgentMessageSerializer extends TypeBasedSerializer[AgentMessage]

  implicit object JobAssignmentSerializer extends JsonSerializer[JobAssignment] {
    private def readMaterials(json: Map[String, Any]) = {
      val material = readObject[Material](json / "material")
      val workset = ChangeSourceSerializer.readWorkset(material.source, json / "workset")
      material -> workset
    }

    def read(json: Map[String, Any]) =
      JobAssignment(json / "pipeline", (json > ("materials", readMaterials)).toMap, readObject[Job](json / "job"))

    def write(assignment: JobAssignment) =
      Map("type" -> "assignment", "pipeline" -> assignment.pipeline,
        "materials" -> assignment.materials.map(keyValue =>
          Map("material" -> writeObject(keyValue._1), "workset" -> ChangeSourceSerializer.writeWorkset(keyValue._1.source, keyValue._2))
          ).toList, "job" -> writeObject(assignment.job))
  }

  implicit object JobAssignmentFutureSerializer extends JsonSerializer[JobAssignmentFuture] {

    private def readMaterial(json: Map[String, Any]): (ChangeSource, String) = {
      val vcs = readObject[ChangeSource](json)
      (vcs, json / "dest")
    }

    private def readMaterialWorkset(json: Map[String, Any]) = {
      val material = readMaterial(json / "material")
      val workset = ChangeSourceSerializer.readWorkset(material._1, json / "workset")
      material -> workset
    }

    def read(json: Map[String, Any]) = JobAssignmentFuture((json / "buildId").asInstanceOf[Double].toInt, json / "pipeline", (json > ("materials", readMaterialWorkset)).toMap,
                                                          json > ("tasks", readObject[Task](_)))

    def write(assignment: JobAssignmentFuture) =
      Map("type" -> "assignment", "buildId" -> assignment.buildId, "pipeline" -> assignment.pipeline,
        "materials" -> assignment.sources.map(keyValue =>
          Map("material" -> (writeObject(keyValue._1._1) + ("dest" -> keyValue._1._2)), "workset" -> ChangeSourceSerializer.writeWorkset(keyValue._1._1, keyValue._2))
          ).toList, "tasks" -> assignment.tasks.map(writeObject(_)))
  }

  implicit object JobCompletedSerializer extends JsonSerializer[JobCompleted] {
    def read(json: Map[String, Any]) = JobCompleted()

    def write(message: JobCompleted) = Map("type" -> "jobcompleted")
  }

  implicit object JobFailedSerializer extends JsonSerializer[JobFailed] {
    def read(json: Map[String, Any]) = JobFailed(json / "reason")

    def write(message: JobFailed) = Map("type" -> "jobfailed", "reason" -> message.reason)
  }
  implicit object ConsoleOutputSerializer extends JsonSerializer[ConsoleOutput] {
    def read(json: Map[String, Any]) = ConsoleOutput(URLDecoder.decode(json / "content", "UTF-8"))

    def write(message: ConsoleOutput) = Map("type" -> "consoleoutput", "content" -> URLEncoder.encode(message.output, "UTF-8"))
  }

  AgentMessageSerializer.register[JobAssignment]("assignment")
  AgentMessageSerializer.register[JobCompleted]("jobcompleted")
  AgentMessageSerializer.register[JobFailed]("jobfailed")
  AgentMessageSerializer.register[ConsoleOutput]("consoleoutput")


  implicit def map2Json(json: Map[String, Any]): JsonHelper = new JsonHelper(json)

  class JsonHelper(val json: Map[String, Any]) {
    def /[T](name: String): T =
      json.get(name) match {
        case Some(result: T) => result
        case _ => throw new Exception(name)
      }

    def /?[T](name: String, default: T): T =
      json.get(name) match {
        case Some(result: T) => result
        case _ => default
      }

    def >[V](name: String, map: Map[String, Any] => V) =
      json.get(name) match {
        case Some(list: List[Map[String, Any]]) => list.map(map)
        case _ => throw new Exception(name)
      }


    def /[V](name: String, map: List[String] => V) =
      json.get(name) match {
        case Some(list: List[String]) => map(list)
        case _ => throw new Exception()
      }
  }
}