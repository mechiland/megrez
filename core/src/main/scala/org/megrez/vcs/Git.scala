package org.megrez.vcs

import org.megrez.runtime.ShellCommand
import java.io.File
import io.Source

class Git(val url: String) extends VersionControl with ShellCommand {
  def changes(workingDir: File): Option[Any] = {
    if (isRepository(workingDir)) update(workingDir, None) else checkout(workingDir, None)
    Some(Source.fromInputStream(run("git log --pretty=%H -1", workingDir).getInputStream).mkString)
  }

  def isRepository(workingDir: File): Boolean = {return workingDir.exists && new File(workingDir + "/.git").exists}

  def checkout(workingDir: File, workSet: Option[Any]) {
    run("git clone " + url + " .", workingDir)
    update(workingDir, workSet)
  }

  def update(workingDir: File, workSet: Option[Any]) {
    if(workSet.eq(None))
      run("git pull ", workingDir)
    else run("git checkout " + workSet.get, workingDir)
  }
}