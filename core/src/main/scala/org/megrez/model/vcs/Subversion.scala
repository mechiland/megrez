package org.megrez.model.vcs

import java.io.File
import xml.XML
import org.megrez.util.ShellCommand

trait Subversion extends VersionControl with ShellCommand {  
  self: {val url: String} =>
  def changes(workingDir: File, previous: Option[Any]) = {
    val current = Integer.parseInt(((XML.load(run("svn info " + url + " --xml").getInputStream) \\ "entry")(0) \ "@revision").text)
    previous match {
      case Some(previous: Int) =>
        if (previous < current) Some(current) else None
      case _ => Some(current)
    }
  }

  def isRepository(workingDir: File): Boolean = check("svn info " + workingDir.getAbsolutePath)

  def checkout(workingDir: File, workSet: Option[Any]) {
    run("svn co " + (workSet match {
      case Some(revision: Int) => url + "@" + revision
      case _ => url
    }) + " " + workingDir.getAbsolutePath)
  }

  def update(workingDir: File, workSet: Option[Any]) {
    run("svn up " + workingDir.getAbsolutePath + (workSet match {
      case Some(revision: Int) => " -r " + revision
      case _ => ""
    }))
  }
}