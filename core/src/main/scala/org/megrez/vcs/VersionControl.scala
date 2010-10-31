package org.megrez.vcs

import java.io.File

trait VersionControl {
  def isRepository(workingDir: File): Boolean

  def changes(workingDir: File): Option[Any]

  def checkout(workingDir: File, workSet: Option[Any])

  def update(workingDir: File, workSet: Option[Any])
}
