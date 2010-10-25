package org.megrez.agent.vcs

import java.io.File

trait VersionControl {
  def initRepository(workingDir: File, workSet : Any)
  def checkout(workingDir: File, workSet : Any)
}