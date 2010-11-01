package org.megrez.io

import org.megrez.vcs.Subversion
import collection.mutable.HashMap
import org.megrez._

trait JSON[T] extends Reader[T, Any]

trait PluggableJSON[T] extends JSON[T] {
  private val factories = HashMap[String, Any => T]()

  def register(jsonType: String, factory: Any => T) {
    factories.put(jsonType, factory)
  }

  def read(json: Any) = {
    json match {
      case content: Map[Any, Any] =>
        content("type") match {
          case jsonType: String => factories(jsonType)(json)
          case _ => throw new Exception()
        }
      case _ => throw new Exception()
    }
  }
}

object JSON {
  def read[T](json: Any)(implicit resource: JSON[T]): T = resource.read(json)

  implicit object MaterialJson extends PluggableJSON[Material]

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
}