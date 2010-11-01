package org.megrez

import java.io.File

trait Material {
  def changes(workingDir: File): Option[Any]  
}