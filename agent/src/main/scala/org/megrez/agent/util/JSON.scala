package org.megrez.agent.util

import org.megrez.agent.model.vcs.{Git, Subversion}
import org.megrez.JobAssignment

object JSON {
  private val JsonParser = org.megrez.util.JSON
  import org.megrez.util.JSON._

  def read[T](json: String)(implicit jsObject: JsonSerializer[T]): T = JsonParser.read(json)
  def write[T](resource: T)(implicit jsObject: JsonSerializer[T]): String = JsonParser.write(resource)

  implicit object AgentSubversionSerializer extends JsonSerializer[Subversion] {
    def read(json: Map[String, Any]) = new Subversion(json / "url")

    def write(resource: Subversion) = Map("type" -> "svn", "url" -> resource.url)

    private val readWorkset: Map[String, Any] => Option[Any] = json => Some((json / "revision").asInstanceOf[Double].toInt)
    private val writeWorkset: Option[Any] => Map[String, Any] = revision => revision match {
      case Some(revision: Int) => Map("revision" -> revision)
      case _ => throw new Exception()
    }

    ChangeSourceSerializer.register[Subversion](readWorkset, writeWorkset)
  }

  implicit object AgentGitSerializer extends JsonSerializer[Git] {
    def read(json: Map[String, Any]) = new Git(json / "url")

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
  AgentMessageSerializer.register[JobAssignment]("assignment")
}