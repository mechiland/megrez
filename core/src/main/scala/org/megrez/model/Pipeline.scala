package org.megrez.model

import java.io.File

trait Task {
  
}

trait ChangeSource {
  def changes(workingDir: File, previous: Option[Any]): Option[Any]
}