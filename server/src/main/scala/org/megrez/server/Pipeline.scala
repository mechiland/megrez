package org.megrez.server

import java.util.{ArrayList, Calendar}
import collection.immutable.HashSet

class Material(val url: String)
class SvnMaterial(override val url: String) extends Material(url)
class GitMaterial(override val url: String) extends Material(url)

trait MaterialSource {
  def parse(json: Map[String, Any]) : Material
}

object SvnMaterial extends MaterialSource{
	def parse(json : Map[String,Any]) = json("url") match {
    	case url : String => new SvnMaterial(url)
    	case _ => null
  	}
}
object GitMaterial extends MaterialSource{
	def parse(json : Map[String,Any]) = json("url") match {
    	case url : String => new GitMaterial(url)
    	case _ => null
  	}
}


object Material {
  def find(vcs : String) = vcs match {
    case "svn" => SvnMaterial
	case "git" => GitMaterial
    case _ => null
  }
}

class PipelineConfig(val name: String, val material: Material, val stages: List[String]) {
  def workingDir() = {
    System.getProperty("user.dir") + "/pipelines" + name
  }
}

class Pipeline(val name: String, val repositoryUrl: String, val buildRevision: String, val buildDate: Calendar = null, val workDir: String = "")

class Job(val name: String, val resources: Set[String], val tasks: List[Task])

class Task

object Configuration {

  def hasNextStage(pipeline: String, stage: String) = {
    false
  }

  def firstStage(pipeline: String) = {
    "stage1"
  }

  def nextStage(pipeline: String, stage: String) = {
    "stage2"
  }
}