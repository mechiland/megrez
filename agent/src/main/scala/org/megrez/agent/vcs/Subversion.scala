package org.megrez.agent.vcs

import java.io.File

class Subversion(val url : String) extends VersionControl {
  
  override def checkout(workingDir: File, workSet : Any) {
    val svnUrl = workSet match {
      case revision : Int => url + "@" + revision 
      case _ => url
    }
    Runtime.getRuntime().exec("svn co " + svnUrl + " " + workingDir.getAbsolutePath).waitFor match {
      case 0 =>
      case _ => 
    }
  }

  def isRepository(workingDir: File) = {
    Runtime.getRuntime().exec("svn info " + workingDir.getAbsolutePath).waitFor match {
      case 0 => true
      case _ => false
    }
  }


  def update(workingDir: File, workSet: Any) {
    val revision = workSet match {
      case revision : Int => " -r " + revision
      case _ => ""
    }
    Runtime.getRuntime().exec("svn up " + workingDir.getAbsolutePath + revision).waitFor match {
      case 0 =>
      case _ =>
    }    
  }
}