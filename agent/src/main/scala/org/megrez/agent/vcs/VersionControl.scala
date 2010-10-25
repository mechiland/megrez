package org.megrez.agent.vcs

import java.io.File

trait VersionControl {
  def checkout(workingDir: File, workSet : Any)
}