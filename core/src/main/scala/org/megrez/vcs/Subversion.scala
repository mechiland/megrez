package org.megrez.vcs

import java.io.File
import xml.XML

class Subversion(val url: String) extends VersionControl {
  def changes(workingDir: File): Option[Any] = {
    val process = Runtime.getRuntime().exec("svn info " + url + " --xml")
    process.waitFor match {
      case 0 =>
        val svnInfo = XML.load(process.getInputStream)
        Some(Integer.parseInt(((svnInfo \\ "entry")(0) \ "@revision").text))
      case _ => None
    }
  }

  def isRepository(workingDir: File): Boolean = {
    val process = Runtime.getRuntime().exec("svn info " + workingDir.getAbsolutePath)
    process.waitFor match {
      case 0 => true
      case _ => false
    }
  }

  def checkout(workingDir: File, workSet: Option[Any]) {
    val svnUrl = workSet match {
      case Some(revision : Int) => url + "@" + revision 
      case _ => url
    }    
    val process = Runtime.getRuntime().exec("svn co " + svnUrl + " " + workingDir.getAbsolutePath)
    process.waitFor match {
      case 0 =>
      case _ =>  
    }
  }

  def update(workingDir: File, workSet: Option[Any]) {
    val revision = workSet match {
      case Some(revision : Int) => " -r " + revision
      case _ => ""
    }

    val process = Runtime.getRuntime().exec("svn up " + workingDir.getAbsolutePath + revision)
    process.waitFor match {
      case 0 =>
      case _ =>
    }
  }
}