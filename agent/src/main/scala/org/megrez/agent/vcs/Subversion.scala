package org.megrez.agent.vcs

import java.io.File

class Subversion(val url : String) extends VersionControl with CommandLine {
  
  override def checkout(workingDir: File, workSet : Any) {
    val svnUrl = workSet match {
      case revision : Int => url + "@" + revision 
      case _ => url
    }
    run("svn co " + svnUrl + " " + workingDir.getAbsolutePath)
  }

  def isRepository(workingDir: File) = {
    check("svn info " + workingDir.getAbsolutePath)
  }


  def update(workingDir: File, workSet: Any) {
    val revision = workSet match {
      case revision : Int => " -r " + revision
      case _ => ""
    }
    run("svn up " + workingDir.getAbsolutePath + revision)
  }


  override def toString = "svn : " + url
}

object Subversion extends VersionControlSource {
  override def parse(json : Map[String,Any]) = json("url") match {
    case url : String => new Subversion(url)
    case _ => null
  }

  override def parseWorkSet(json : Map[String, Any]) = json("revision") match {
    case revision : Number => revision.intValue
    case revision : String => Integer.parseInt(revision)
    case _ => null
  }
}