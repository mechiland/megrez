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
}