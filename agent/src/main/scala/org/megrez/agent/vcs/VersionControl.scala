package org.megrez.agent.vcs

import java.io.File
import io.Source

trait VersionControl {
  def isRepository(workingDir: File): Boolean

  def checkout(workingDir: File, workSet: Any)

  def update(workingDir: File, workSet: Any)
}

trait VersionControlSource {
  def parse(json : Map[String,Any]) : VersionControl
  def parseWorkSet(json : Map[String, Any]) : Any
}

object VersionControl {
  def find(vcs : String) = vcs match {
    case "svn" => Subversion
    case _ => null
  }
}

class VersionControlException(val message: String) extends Exception(message)

trait CommandLine {
  def check(cmd: String) = {
    val process: Process = Runtime.getRuntime().exec(cmd)
    process.waitFor match {
      case 0 => true
      case _ => false
    }
  }

  def run(cmd: String) {
    val process: Process = Runtime.getRuntime().exec(cmd)
    process.waitFor match {
      case 0 =>
      case _ => throw new VersionControlException(Source.fromInputStream(process.getErrorStream).mkString)
    }
  }
}