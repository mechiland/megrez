package org.megrez.agent.vcs

import java.io.File

trait VersionControl {
  def isRepository(workingDir: File) : Boolean
  def checkout(workingDir: File, workSet : Any)
  def update(workingDir: File, workSet : Any)
}

class VersionControlException(val message : String) extends Exception(message)