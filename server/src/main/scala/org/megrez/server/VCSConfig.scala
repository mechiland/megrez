package org.megrez.server

import collection.mutable.{HashMap, HashSet}

class Material(val url: String)
class SvnMaterial(override val url: String) extends Material(url)
class GitMaterial(override val url: String) extends Material(url)

trait MaterialSource {
  def parse(json: Map[String, Any]): Material
}

object SvnMaterial extends MaterialSource {
  def parse(json: Map[String, Any]) = json("url") match {
    case url: String => new SvnMaterial(url)
    case _ => null
  }
}
object GitMaterial extends MaterialSource {
  def parse(json: Map[String, Any]) = json("url") match {
    case url: String => new GitMaterial(url)
    case _ => null
  }
}


object Material {
  def find(vcs: String) = vcs match {
    case "svn" => SvnMaterial
    case "git" => GitMaterial
    case _ => null
  }
}