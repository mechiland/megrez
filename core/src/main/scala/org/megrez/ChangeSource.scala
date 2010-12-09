package org.megrez

import java.io.File

trait ChangeSource {
  def changes(workingDir: File, previous: Option[Any]): Option[Any]
}
