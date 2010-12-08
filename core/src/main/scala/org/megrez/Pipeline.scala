package org.megrez

import java.io.File

trait Task {
  def execute(workingDir: File): String

  def cancel()
}

trait ChangeSource {
  def changes(workingDir: File, previous: Option[Any]): Option[Any]
}
