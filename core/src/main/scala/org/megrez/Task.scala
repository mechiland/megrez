package org.megrez

import java.io.File

trait Task {
  def execute(workingDir: File): String

  def cancel()
}