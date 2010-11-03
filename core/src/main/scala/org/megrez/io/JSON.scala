package org.megrez.io

import org.megrez.vcs.Subversion
import collection.mutable.HashMap
import org.megrez._
import java.util.UUID

trait JSON[T] extends Reader[T, Any]

trait PluggableJSON[T] extends JSON[T] {
  private val parser = HashMap[String, Any => T]()

  def register(jsonType: String, factory: Any => T) {
    parser.put(jsonType, factory)
  }

  def read(json: Any) = {
    json match {
      case content: Map[Any, Any] =>
        content("type") match {
          case jsonType: String => parser(jsonType)(json)
          case _ => throw new Exception()
        }
      case _ => throw new Exception()
    }
  }
}

object JSON {
  def read[T](json: Any)(implicit resource: JSON[T]): T = resource.read(json)

  implicit object MaterialJson extends PluggableJSON[Material] {
    private val workSetParsers = HashMap[String, Any => Option[Any]]()

    def registerWorkSet(jsonType: String, factory: Any => Option[Any]) {
      workSetParsers.put(jsonType, factory)
    }

    def readWorkSet(jsonType: String, json: Any) = json match {
      case content: Map[Any, Any] =>
        workSetParsers.get(jsonType) match {
          case Some(parser) => parser(json)
          case _ => None
        }
      case _ => throw new Exception()
    }
  }

  implicit object PipelineJson extends JSON[Pipeline] {
    def read(json: Any) = {
      json match {
        case content: Map[Any, Any] =>
          val name = content("name") match {
            case name: String => name
            case _ => throw new Exception()
          }
          val materials = content("materials") match {
            case materials: List[Any] => materials.map(JSON.read[Material](_)).toSet
            case _ => throw new Exception()
          }
          new Pipeline(name, materials, List[Pipeline.Stage]())
        case _ => throw new Exception()
      }
    }
  }

  MaterialJson.register("svn", json =>
    json match {
      case content: Map[Any, Any] =>
        content("url") match {
          case url: String => new Subversion(url)
          case _ => throw new Exception()
        }
      case _ => throw new Exception()
    }
    )

  MaterialJson.registerWorkSet("svn", json =>
    json match {
      case content: Map[Any, Any] =>
        content("revision") match {
          case revision: String => Some(Integer.parseInt(revision))
          case _ => throw new Exception()
        }
      case _ => throw new Exception()
    }
    )

  implicit object JobAssignmentJson extends JSON[JobAssignment] {
    def read(json: Any) = {
      json match {
        case content: Map[Any, Any] =>
          val build = content("build") match {
            case uuid: String => UUID.fromString(uuid)
            case _ => throw new Exception()
          }
          val materials = content("materials") match {
            case materials: List[Map[Any, Map[Any, Any]]] =>
              materials.map(material =>
                JSON.read[Material](material("material")) -> (material("material")("type") match {
                  case materialType: String =>
                    MaterialJson.readWorkSet(materialType, material("workset"))
                  case _ => throw new Exception()
                })).toMap
            case _ => throw new Exception()
          }
          JobAssignment(build, materials, null)
        case _ => throw new Exception()
      }
    }
  }
}