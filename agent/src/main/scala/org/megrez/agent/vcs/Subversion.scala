package org.megrez.agent.vcs

import java.io.File

class Subversion(val url : String) extends VersionControl {
  
  override def checkout(workingDir: File, workSet : Any) {
    val svnUrl = workSet match {
      case revision : Int => url + "@" + revision 
      case _ => url
    }
    val process = Runtime.getRuntime().exec("svn co " + svnUrl + " " + workingDir.getAbsolutePath)    
    process.waitFor match {
      case 0 =>
      case _ => 
    }
  }
}