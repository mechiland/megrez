package org.megrez.server

import _root_.main.scala.org.megrez.server.trigger.VersionControl
import trigger.{Git, Svn}
import java.io.File

abstract class Material(val url: String) {
  def versionControl(workingDir: File): VersionControl
}
class SvnMaterial(override val url: String) extends Material(url) {
  override def versionControl(workingDir: File) = new Svn(url)
}
class GitMaterial(override val url: String) extends Material(url) {
  override def versionControl(workingDir: File) = new Git(url, workingDir)
}

object Material {
  def parse(vcs: String, json: Map[String, Any]) = vcs match {
    case "svn" => SvnMaterial.parse(json)
    case "git" => GitMaterial.parse(json)
    case _ => null
  }
}

object SvnMaterial {
  def parse(json: Map[String, Any]) = json("url") match {
    case url: String => new SvnMaterial(url)
    case _ => null
  }
}
object GitMaterial {
  def parse(json: Map[String, Any]) = json("url") match {
    case url: String => new GitMaterial(url)
    case _ => null
  }
}
