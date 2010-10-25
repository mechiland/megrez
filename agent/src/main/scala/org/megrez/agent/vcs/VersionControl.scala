package org.megrez.agent.vcs

import java.io.File

trait VersionControl {
  def checkWorkingDir(workingDir: File) : Boolean
  def checkout(workingDir: File, workSet : Any)
}