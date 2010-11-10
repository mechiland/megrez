package org.megrez.util

import collection.mutable.HashMap
import org.megrez._
import vcs.Subversion

object JSON {
  private val JsonParser = scala.util.parsing.json.JSON

  def read[T](json: String)(implicit jsObject: JavaScriptObject[T]): T = JsonParser.parseFull(json) match {
    case Some(json: Map[String, Any]) => jsObject.read(json)
    case _ => throw new Exception
  }

  def write[T](resource: T)(implicit jsObject: JavaScriptObject[T]): String = {
    def toJson(obj: Map[String, Any]): String =
      obj.map(keyValue =>
        keyValue._2 match {
          case string: String => "\"" + keyValue._1 + "\":\"" + string + "\""
          case map: Map[String, Any] => "\"" + keyValue._1 + "\":" + toJson(map)
          case list: List[Map[String, Any]] => list.map(toJson).mkString("[", ",", "]")
        }).mkString("{", ",", "}")
    toJson(jsObject.write(resource))
  }

  private def readObject[T](json: Map[String, Any])(implicit jsObject: JavaScriptObject[T]): T = jsObject.read(json)

  private def writeObject[T](resource: T)(implicit jsObject: JavaScriptObject[T]): Map[String, Any] = jsObject.write(resource)

  trait JavaScriptObject[T] extends Serializer[T, Map[String, Any]]

  trait TypeBasedJavaScriptObject[T <: AnyRef] extends JavaScriptObject[T] {
    private val parsers = HashMap[String, JavaScriptObject[_ <: T]]()
    private val writers = HashMap[Class[_], JavaScriptObject[_ <: T]]()

    def register[U <: T](jsonType: String)(implicit m: Manifest[U], handler: JavaScriptObject[U]) {
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
        case Some(writer) => writer.asInstanceOf[JavaScriptObject[T]].write(resource)
        case None => throw new Exception()
      }
  }

  implicit object ChangeSourceJavaScriptObject extends TypeBasedJavaScriptObject[ChangeSource]

  implicit object MaterialJavaScriptObject extends JavaScriptObject[Material] {
    def read(representation: Map[String, Any]) = new Material(JSON.readObject[ChangeSource](representation), representation / "dest")

    def write(resource: Material): Map[String, Any] = JSON.writeObject[ChangeSource](resource.source) ++ Map("dest" -> resource.destination)
  }
  
  implicit object SubversionJavaScriptObject extends JavaScriptObject[Subversion] {
    def read(representation: Map[String, Any]) = new Subversion(representation / "url")

    def write(resource: Subversion) = Map("type" -> "svn", "url" -> resource.url)
  }

  ChangeSourceJavaScriptObject.register[Subversion]("svn")

  implicit def map2Json(json: Map[String, Any]): JsonHelper = new JsonHelper(json)

  class JsonHelper(val json: Map[String, Any]) {
    def /[T](name: String): T = {
      json(name) match {
        case result: T => result
        case _ => throw new Exception()
      }
    }

    def >[V](name: String, map: Map[String, Any] => V) = {
      json(name) match {
        case list: List[Map[String, Any]] =>
          list.map(map)
        case _ => throw new Exception()
      }
    }
  }
}