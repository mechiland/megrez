package org.megrez.vcs

import java.io.File
import xml.XML
import org.megrez.runtime.ShellCommand

class Subversion(val url: String) extends VersionControl with ShellCommand {
  def changes(workingDir: File): Option[Any] =
    Some(Integer.parseInt(((XML.load(run("svn info " + url + " --xml").getInputStream) \\ "entry")(0) \ "@revision").text))

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
