package org.megrez.vcs

import java.io.File
import io.Source
import org.megrez.util.{Logging, ShellCommand}

class Git(val url: String) extends VersionControl with ShellCommand with Logging {
  def changes(workingDir: File, previous: Option[Any]): Option[Any] = {
    if (isRepository(workingDir)) update(workingDir, None) else checkout(workingDir, None)
    val current = Source.fromInputStream(run("git log --pretty=%H -1", workingDir).getInputStream).mkString.trim
    previous match {
      case Some(previous: String) =>
        if (previous != current) Some(current) else None
      case _ => Some(current)
    }
  }

  def isRepository(workingDir: File): Boolean = {return workingDir.exists && new File(workingDir + "/.git").exists}

  def checkout(workingDir: File, workSet: Option[Any]) {
    info ("Checkout git repository from " + url)
    run("git clone " + url + " .", workingDir)
    if (!workSet.eq(None)) update(workingDir, workSet)
  }

  def update(workingDir: File, workSet: Option[Any]) {
    if (workSet.eq(None))
      run("git pull ", workingDir)
    else {
      run("git checkout " + workSet.get, workingDir)
    }
  }
}