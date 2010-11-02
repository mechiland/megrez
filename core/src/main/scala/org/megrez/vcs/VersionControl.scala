package org.megrez.vcs

import java.io.File
import org.megrez.Material

trait VersionControl extends Material {
  def isRepository(workingDir: File): Boolean

  def checkout(workingDir: File, workSet: Option[Any])

  def update(workingDir: File, workSet: Option[Any])
}
