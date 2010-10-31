package org.megrez.server

import collection.mutable.{HashMap, HashSet}
import vcs.VersionControl

class Material(val url: String) extends VersionControl
class SvnMaterial(val url: String) extends VersionControl
class GitMaterial(val url: String) extends VersionControl

object Material {
  def parse(vcs: String, json: Map[String, Any]) = vcs match {
    case "svn" => SvnMaterial.parse(json)
    case "git" => GitMaterial.parse(json)
    case _ => null
  }
}

object SvnMaterial{
  def parse(json: Map[String, Any]) = json("url") match {
    case url: String => new SvnMaterial(url)
    case _ => null
  }
}
object GitMaterial{
  def parse(json: Map[String, Any]) = json("url") match {
    case url: String => new GitMaterial(url)
    case _ => null
  }
}
