package org.megrez.model.vcs

import org.megrez.model.ChangeSource
import java.io.File

trait VersionControl extends ChangeSource {

  def isRepository(workingDir: File): Boolean

  def checkout(workingDir: File, workSet: Option[Any])

  def update(workingDir: File, workSet: Option[Any])

}