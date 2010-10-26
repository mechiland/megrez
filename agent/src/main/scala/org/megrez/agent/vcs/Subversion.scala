package org.megrez.agent.vcs

import java.io.File
import io.Source

class Subversion(val url : String) extends VersionControl {
  
  override def checkout(workingDir: File, workSet : Any) {
    val svnUrl = workSet match {
      case revision : Int => url + "@" + revision 
      case _ => url
    }
    val process = Runtime.getRuntime().exec("svn co " + svnUrl + " " + workingDir.getAbsolutePath)
    process.waitFor match {
      case 0 =>
      case _ => throw new VersionControlException(Source.fromInputStream(process.getErrorStream).mkString)
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