package org.megrez.vcs

import java.io.File
import org.megrez.ChangeSource

trait VersionControl extends ChangeSource {
  var revisionString: String = _

  def isRepository(workingDir: File): Boolean

  def checkout(workingDir: File, workSet: Option[Any])

  def update(workingDir: File, workSet: Option[Any])
}
